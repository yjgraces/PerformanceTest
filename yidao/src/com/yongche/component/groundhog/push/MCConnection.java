package com.yongche.component.groundhog.push;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.client.GroundhogClient;
import com.yongche.component.groundhog.client.ManagerClient;
import com.yongche.component.groundhog.client.WorkerClient;

/**
 * copy of com.yongche.component.groundhog.push.McPersistentConnection
 * 
 * @author luoyanying
 * @date 2016骞�鏈�5鏃�
 */
public class MCConnection {
	private Lock connectionLock;
	public WorkerClient connection;

	@SuppressWarnings("unused")
	private McSessionStorage mcSessionStorage;

	private String currentNetType;

	public long lastRecvPkgTime;

	public static int callDaemonCount = 0;
	private McSessionInfo mcSession;

	public McSessionInfo getMcSession() {
		return mcSession;
	}

	public MCConnection(McSessionStorage mcSessionStrage) {
		this.connectionLock = new ReentrantLock();
		this.mcSessionStorage = mcSessionStrage;
		this.lastRecvPkgTime = 0;
	}

	public boolean retrieveWorkerInfo() throws MessageException, IOException, InterruptedException, PushConnectionException, JSONException {
		// McSessionInfo mcSession = this.mcSessionStorage.getSessionInfo();
		boolean workerChanged = false;
		JSONArray managerHosts;

		if (mcSession.managerHosts == "") {
			throw new PushConnectionException("There is no valid manager hosts");
		} else {
			managerHosts = new JSONArray(mcSession.managerHosts);
		}

		long now = System.currentTimeMillis();
		if (mcSession.workerExpireTime > now && (mcSession.workerPort > 0) && (mcSession.workerHost != null && mcSession.workerHost.length() > 0)) {
			return workerChanged;
		}

		ManagerClient managerConn = GroundhogClient.buildManagerClient();
		managerConn.connectTimeout = CommonConfig.CONN_TIMEOUT;
		managerConn.responseTimeout = CommonConfig.RESPONSE_TIMEOUT;
		managerConn.port = CommonConfig.MANAGER_PORT;

		managerConn.userType = mcSession.userType;
		managerConn.userId = mcSession.userId;
		managerConn.deviceId = mcSession.deviceId;

		for (int i = 0; i < managerHosts.length(); i++) {
			for (int j = 0; j < CommonConfig.MAX_RETRY_MANAGER_TIMES; j++) {
				// if (!NetworkTool.isNetworkUsable(this.bgService)) {
				// break;
				// }

				String[] str = managerHosts.getString(i).split(":", 2);
				if (str.length != 2) {
					break;
				}
				managerConn.host = str[0];
				managerConn.port = Integer.parseInt(str[1]);

				try {
					managerConn.connect();
					break;
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					managerConn.close();
					break;
				} catch (Exception e) {
					e.printStackTrace();
					managerConn.close();
					Thread.sleep(CommonConfig.MANAGER_RETRY_INTERVAL);
				}
			}

			if (managerConn.isConnected()) {
				break;
			}
		}

		if (!managerConn.isConnected()) {
			throw new PushConnectionException("Push Manager Host can't be connected, manager hosts count is " + managerHosts.length());
		}

		try {
			// add by sj
			this.currentNetType = "wifi";// NetworkTool.getCurrentNetType(this.bgService);
			// add by sj
			managerConn.login(mcSession.sessionId, this.currentNetType);
			managerConn.requestWorker();

			mcSession.workerExpireTime = now + managerConn.getMaxAge();
			mcSession.workerHost = managerConn.getWorkerIp();
			mcSession.workerPort = managerConn.getWorkerPort();

			/*
			 * if ( !this.mcSessionStorage.updatezSessionInfo(mcSession) ) {
			 * //Can't verify it works
			 * this.mcSessionStorage.updatezSessionInfo(null);
			 * 
			 * throw new PushConnectionException(
			 * "Push Worker Info can't be saved"); }
			 */
		} finally {
			managerConn.close();
		}

		return workerChanged;
	}

	public void makeWorkerConnection() throws IOException, MessageException, InterruptedException, PushConnectionException {
		// McSessionInfo mcSession = this.mcSessionStorage.getSessionInfo();

		WorkerClient workerConn = GroundhogClient.buildWorkerClient();
		workerConn.connectTimeout = CommonConfig.CONN_TIMEOUT;
		workerConn.responseTimeout = CommonConfig.RESPONSE_TIMEOUT;
		workerConn.host = mcSession.workerHost;
		workerConn.port = mcSession.workerPort;

		workerConn.userType = mcSession.userType;
		workerConn.userId = mcSession.userId;
		workerConn.deviceId = mcSession.deviceId;

		for (int i = 0; i < CommonConfig.MAX_RETRY_WORKER_TIMES; ++i) {
			try {
				workerConn.connect();
				long serverTime = workerConn.login(mcSession.sessionId, this.currentNetType);
				System.out.println("server time is:" + serverTime + " " + mcSession.userId + " logined");
				long timeDeviation = Math.abs(serverTime - System.currentTimeMillis());
				if (timeDeviation > CommonConfig.TIME_CORRECTION_THRESHOLD) {
					this.broadcastTimeCorrection(timeDeviation);
				}
				break;
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				workerConn.close();
			} catch (Exception e) {
				e.printStackTrace();
				workerConn.close();
				Thread.sleep(CommonConfig.WORKER_RETRY_INTERVAL);
			}
		}

		if (!workerConn.isConnected()) {
			mcSession.workerExpireTime = 0;
			mcSession.workerHost = "";
			mcSession.workerPort = 0;
			// this.mcSessionStorage.updatezSessionInfo(mcSession);
			throw new PushConnectionException("Push Worker Host " + workerConn.host + ":" + workerConn.port + " can't be connected");
		}

		Logger.info(this.getClass().getName(), "Connect to worker :" + workerConn.host + ":" + workerConn.port + " sucess" + ", Thread : " + Thread.currentThread().getId());

		try {
			connectionLock.lockInterruptibly();
			this.connection = workerConn;
		} catch (InterruptedException ex) {
			workerConn.close();
			throw ex;
		} finally {
			connectionLock.unlock();
		}
	}

	public void setSessionInfo(String Hosts, long workerExpireTime, String userType, long userId, long deviceId, String token) {
		mcSession = new McSessionInfo();
		mcSession.managerHosts = Hosts;
		mcSession.workerExpireTime = workerExpireTime;
		mcSession.userType = userType;
		mcSession.userId = userId;
		mcSession.deviceId = deviceId;
		mcSession.sessionId = token;
	}

	private void broadcastTimeCorrection(long td) {

	}

	public void requestPost(String url, List<NameValuePair> params) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();

		HttpPost httppost = new HttpPost(url);
		httppost.setEntity(new UrlEncodedFormEntity(params));

		CloseableHttpResponse response = httpclient.execute(httppost);
		System.out.println(response.toString());

		HttpEntity entity = response.getEntity();
		String jsonStr = EntityUtils.toString(entity, "utf-8");
		System.out.println(jsonStr);

		httppost.releaseConnection();
	}

	public void close() {
		connectionLock.lock();
		try {
			if (this.connection != null) {
				this.connection.close();
				this.connection = null;
			}
		} finally {
			connectionLock.unlock();
		}
	}

}
