package com.yongche.component.groundhog.push;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.client.ClientException;
import com.yongche.component.groundhog.client.GroundhogClient;
import com.yongche.component.groundhog.client.ManagerClient;
import com.yongche.component.groundhog.client.WorkerClient;
import com.yongche.component.groundhog.message.AcknowledgeMessage;
import com.yongche.component.groundhog.message.GroundhogMessage;
import com.yongche.component.groundhog.message.GroundhogMessageException;
import com.yongche.component.groundhog.message.PingMessage;
import com.yongche.component.groundhog.message.PushRequestMessage;
import com.yongche.component.groundhog.message.RpcRequestMessage;
import com.yongche.component.groundhog.message.RpcResponseMessage;
import com.yongche.component.groundhog.message.SubscribeRequestMessage;
import com.yongche.component.groundhog.message.UnsubscribeRequestMessage;

public class McPersistentConnection extends ActionHandler {
    private Service bgService;
    
    private Lock connectionLock;
    public WorkerClient connection;
    
    private McSessionStorage mcSessionStorage;
    
    private String currentNetType;

    public long lastRecvPkgTime;
    
    public static int callDaemonCount = 0;
    private McSessionInfo mcSession;
    
    public McPersistentConnection(Service bgService,
            McSessionStorage mcSessionStrage) {
        super();
        this.bgService = bgService;
        this.connectionLock = new ReentrantLock();
        this.mcSessionStorage = mcSessionStrage;
        this.lastRecvPkgTime = 0;
    }
    
    protected void handleIntent(Intent i) throws PushConnectionException, MessageException, IOException, InterruptedException, JSONException {
    	Bundle bdl = null;
    	boolean launchDaemon = false;
    	
    	String action = i.getAction();
        if (action.equals(PushService.ACTION_INIT)) {
            McSessionInfo mcSession = this.mcSessionStorage.getSessionInfo();
            bdl = i.getExtras();
            if (bdl == null) {
            	return;
            }
            launchDaemon = this.needLaunch(bdl, mcSession);
        	
            JSONArray managerHosts = new JSONArray(bdl.getStringArrayList("mc_manager_hosts"));
            mcSession.managerHosts = managerHosts.toString();
            
            if ((mcSession.userType == null || !bdl.getString("mc_user_type").equals(mcSession.userType))
            		|| (bdl.getLong("mc_device_id") != mcSession.deviceId)) {
            	mcSession.workerHost = "";
            	mcSession.workerPort = 0;
            }
            
            mcSession.userType = bdl.getString("mc_user_type");
            mcSession.userId = bdl.getLong("mc_user_id");
            mcSession.deviceId = bdl.getLong("mc_device_id");
            mcSession.sessionId = bdl.getString("mc_session_id");
            this.mcSessionStorage.updatezSessionInfo(mcSession);
        }
        
        if (NetworkTool.isNetworkUsable(this.bgService)) {
            this.currentNetType = NetworkTool.getCurrentNetType(this.bgService);
            this.retrieveWorkerInfo();
            this.makeWorkerConnection();
            
            if (launchDaemon) {
            	checkLaunchDaemon(bdl);
            }
            
            this.recvLoop();
        }
        return;
    }

    public void requestStop() {
        super.requestStop();
        this.close();
        return;
    }
    
    private boolean needLaunch(Bundle bdl, McSessionInfo mcSession)
    {
    	if (callDaemonCount == 0) {
    		return true;
    	}
    	
    	@SuppressWarnings("unchecked")
		ArrayList<String> managerHosts = bdl.getStringArrayList("mc_manager_hosts");
        String userType = bdl.getString("mc_user_type");
        long userId = bdl.getLong("mc_user_id");
        long deviceId = bdl.getLong("mc_device_id");
        String sessionId = bdl.getString("mc_session_id");
        
        if (managerHosts == null || managerHosts.isEmpty()) {
        	return false;
        }
        if (userType == null || deviceId == 0 || sessionId == null) {
        	return false;
        }
        
        if (mcSession.userType == null || mcSession.sessionId == null
        		|| mcSession.managerHosts == null) {
        	return true;
        }
        
        JSONArray hosts = new JSONArray(managerHosts);
        if (!userType.equals(mcSession.userType)) {
        	return true;
        }
        if (!sessionId.equals(mcSession.sessionId)) {
        	return true;
        }
        if (!hosts.toString().equals(mcSession.managerHosts)) {
        	return true;
        }
        
        return (userId != mcSession.userId || deviceId != mcSession.deviceId);
    }
    
    private void checkLaunchDaemon(Bundle inBundle)
    {
    	Logger.info(this.getClass().getName(), "before launch daemon");
    	callDaemonCount++;
    	@SuppressWarnings("unchecked")
		ArrayList<String> managerHosts = inBundle.getStringArrayList("mc_manager_hosts");
        String userType = inBundle.getString("mc_user_type");
        long userId = inBundle.getLong("mc_user_id");
        long deviceId = inBundle.getLong("mc_device_id");
        String sessionId = inBundle.getString("mc_session_id");
        String slaveManager;
        
        if (managerHosts.size() >= 2) {
        	slaveManager = managerHosts.get(1);
        } else {
        	slaveManager = managerHosts.get(0);
        }
        
        Intent i = new Intent(this.bgService, PushService.class);
        i.setAction(PushService.ACTION_INIT);
        Bundle bdl = new Bundle();
        bdl.putString("master", managerHosts.get(0));
        bdl.putString("slave", slaveManager);
        bdl.putString("user_type", userType);
        bdl.putLong("user_id", userId);
        bdl.putLong("device_id", deviceId);
        bdl.putString("token", sessionId);
        i.putExtras(bdl);
        
		//Daemon.run(this.bgService, PushService.class, i);
		Logger.info(this.getClass().getName(), "end launch daemon");
    }
    
    public void setSessionInfo(String Hosts,long workerExpireTime,
    		String userType,long userId,long deviceId,String token)
    {
    	mcSession = new McSessionInfo();
    	mcSession.managerHosts = Hosts;
    	mcSession.workerExpireTime = workerExpireTime;
    	mcSession.userType = userType;
    	mcSession.userId = userId;
    	mcSession.deviceId = deviceId;
    	mcSession.sessionId = token;
    }
    public boolean retrieveWorkerInfo() throws MessageException, IOException, InterruptedException, PushConnectionException, JSONException {
        //McSessionInfo mcSession = this.mcSessionStorage.getSessionInfo();
        boolean workerChanged = false;
        JSONArray managerHosts;

        if (mcSession.managerHosts == "") {
            throw new PushConnectionException("There is no valid manager hosts");
        } else {
            managerHosts = new JSONArray(mcSession.managerHosts);
        }
        
        long now = System.currentTimeMillis();
        if (!PushService.subscribeEnabled) {
            //worker host cache hasn't been expired
            if (mcSession.workerExpireTime > now && (mcSession.workerPort > 0) &&
            		(mcSession.workerHost != null && mcSession.workerHost.length() > 0)) {
                return workerChanged;
            }
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
        //        if (!NetworkTool.isNetworkUsable(this.bgService)) {
        //            break;
        //        }

                String[] str = managerHosts.getString(i).split(":", 2);
                if (str.length != 2) {
                    break;
                }
                managerConn.host = str[0];
                managerConn.port = Integer.parseInt(str[1]);

                try {
                    managerConn.connect();
                    break;
                } catch(SocketTimeoutException e) {
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
        	//add by sj
            this.currentNetType = "wifi";//NetworkTool.getCurrentNetType(this.bgService);
            //add by sj
	        managerConn.login(mcSession.sessionId, this.currentNetType);
	        if (PushService.subscribeEnabled &&
	                PushService.subscribeInfo.driverId != 0)
	        {
	            managerConn.userType = GroundhogMessage.USER_TYPE_DRIVER;
	            managerConn.userId = PushService.subscribeInfo.driverId;
	            managerConn.deviceId = 0;
	        }
	        managerConn.requestWorker();

	        if (PushService.subscribeEnabled) {
	            if (!managerConn.getWorkerIp().equals(mcSession.workerHost) ||
	                    managerConn.getWorkerPort() != mcSession.workerPort)
	            {
	                workerChanged = true;
	                Logger.info(this.getClass().getName(), "driver in different worker");
	            }
	        }
	        mcSession.workerExpireTime = now + managerConn.getMaxAge();
	        mcSession.workerHost = managerConn.getWorkerIp();
	        mcSession.workerPort = managerConn.getWorkerPort();

         /*   if ( !this.mcSessionStorage.updatezSessionInfo(mcSession) ) {
                //Can't verify it works
                this.mcSessionStorage.updatezSessionInfo(null);
                
                throw new PushConnectionException("Push Worker Info can't be saved");
            }*/
        } finally {
            managerConn.close();
        }

        return workerChanged;
    }
    
    public void makeWorkerConnection() throws IOException, MessageException, InterruptedException, PushConnectionException {
        //McSessionInfo mcSession = this.mcSessionStorage.getSessionInfo();
        
        WorkerClient workerConn = GroundhogClient.buildWorkerClient();
        workerConn.connectTimeout = CommonConfig.CONN_TIMEOUT;
        workerConn.responseTimeout = CommonConfig.RESPONSE_TIMEOUT;
        workerConn.host = mcSession.workerHost;
        workerConn.port = mcSession.workerPort;
        
        workerConn.userType = mcSession.userType;
        workerConn.userId = mcSession.userId;
        workerConn.deviceId = mcSession.deviceId;

        for(int i = 0; i < CommonConfig.MAX_RETRY_WORKER_TIMES; ++i) {
            try {
                workerConn.connect();
                long serverTime = workerConn.login(mcSession.sessionId, this.currentNetType);
    			System.out.println("server time is:" + serverTime);
                long timeDeviation = Math.abs(serverTime - System.currentTimeMillis());
                if (timeDeviation > CommonConfig.TIME_CORRECTION_THRESHOLD) {
                    this.broadcastTimeCorrection(timeDeviation);
                }
                break;
            } catch(SocketTimeoutException e) {
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
            //this.mcSessionStorage.updatezSessionInfo(mcSession);
            throw new PushConnectionException("Push Worker Host "
            		+ workerConn.host + ":" + workerConn.port + " can't be connected");
        }
        
        Logger.info(this.getClass().getName(), "Connect to worker :"
                + workerConn.host + ":"+workerConn.port + " sucess" +
                ", Thread : " + Thread.currentThread().getId());
        
        try {
            connectionLock.lockInterruptibly();
            this.connection = workerConn;
            if (PushService.subscribeEnabled) {
                this.connection.subscribe(PushService.subscribeInfo.driverId,
                        PushService.subscribeInfo.orderId, PushService.subscribeInfo.messageType,
                        PushService.subscribeInfo.retryTimeout);
            }
        }
        catch (InterruptedException ex) {
            workerConn.close();
            throw ex;
        } finally {
            connectionLock.unlock();
        }
    }

    private void recvLoop() throws IOException, MessageException {
        try {
            this.broadcastConnectionStatus(true);
            ((PushService)this.bgService).notifyPushServiceActionHandler();
            while (!this.requestStopFlag) {
                this.handle();
            }
        } finally {
            this.close();
            PushService.clearWaitingResponse();
            this.broadcastConnectionStatus(false);
        }
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

    public boolean isConnected() {
        boolean isConnected = false;
        connectionLock.lock();
        try {
            isConnected = this.connection != null && this.connection.isConnected();
        } finally {
            connectionLock.unlock();
        }
        return isConnected;
    }

    public void ping() throws ClientException {
         this.connection.ping(CommonConfig.PING_TIMEOUT);
    }
    
    public void sendMsg(String message, short messageType, long expire,
            long seq, boolean enableEncryption, boolean disableCompression) throws ClientException {
        this.connection.sendMsg(message, messageType, expire, seq, enableEncryption, disableCompression);
    }
    
    public void rpcRequest(String serviceType, String serviceUri, String data,
            long expire, long seq, boolean enableEncryption, boolean disableCompression) throws ClientException {
        this.connection.rpcRequest(serviceType, serviceUri, data, expire, seq, enableEncryption, disableCompression);
    }

    public void subscribe(long driverId, long orderId, short messageType, long retryTimeout)
            throws ClientException, PushConnectionException, MessageException, IOException, InterruptedException, JSONException {
        PushService.subscribeInfo.driverId = driverId;
        PushService.subscribeInfo.orderId = orderId;
        PushService.subscribeInfo.messageType = messageType;
        PushService.subscribeInfo.retryTimeout = retryTimeout;
        PushService.subscribeEnabled = true;

        try {
            if (this.retrieveWorkerInfo()) {
                if (this.isConnected()) {
                    Logger.info(this.getClass().getName(), "Close connection due to "
                            + "driver in different worker");
                    this.close();
                }
            } else {
                Logger.info(this.getClass().getName(), "passenger and driver are in same worker");
                if (this.isConnected()) {
                    this.connection.subscribe(driverId, orderId, messageType, retryTimeout);
                }
            }
        }
        catch (ClientException ex) {
            Logger.info(this.getClass().getName(), "driver auth failed");
            PushService.subscribeEnabled = false;
        }
    }

    public void unsubscribe(long driverId, long orderId, short messageType) throws ClientException {
        if (this.isConnected()) {
            this.connection.unsubscribe(driverId, orderId,
                    messageType, CommonConfig.SUBSCRIBE_TIMEOUT / 1000);
        }
        PushService.subscribeEnabled = false;
    }

    public void requestReconnect(Intent i) {
        this.addAction(i);
    }

    public synchronized void addAction(Intent i) {
        String action = i.getAction();
        
        if ( action.equals(PushService.ACTION_INIT) ) {
            if (!this.actionQueue.isEmpty()) {
                this.actionQueue.clear();
            }
            
            this.actionQueue.add(i);
        } else {
            Intent currentIntent = this.actionQueue.peek();
            if (currentIntent == null) {
                this.actionQueue.add(i);
            }
            else if (!currentIntent.getAction().equals(action) ) {
                this.actionQueue.clear();
                this.actionQueue.add(i);
            }
        }
    }
    
    public void broadcastConnectionStatus(boolean connectionStatus) {
        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_CONNECTION_STATUS);
        intent.putExtra("status", connectionStatus);
        //broadcast the push message
        this.bgService.sendBroadcast(intent);
    }

    public void broadcastPushMessage(String message) {
        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_RECEIVED_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        intent.putExtras(bundle);
        this.bgService.sendBroadcast(intent);
    }
    
    public void broadcastTimeCorrection(Long timeDeviation) {
        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_TIME_CORRECTION);
        Bundle bundle = new Bundle();
        bundle.putLong("time_deviation", timeDeviation);
        intent.putExtras(bundle);
        this.bgService.sendBroadcast(intent);
    }
    
    public void broadcastRpcResponse(long sequenceId, String result) {
        Intent intent = new Intent();
        intent.setAction(PushService.ACTION_RPC_RESPONSE);
        Bundle bundle = new Bundle();
        bundle.putLong("sequence_id", sequenceId);
        bundle.putString("result", result);
        intent.putExtras(bundle);
        this.bgService.sendBroadcast(intent);
    }

    public void printCurrentThreadStackTrace()
    {
        Logger.info(this.getClass().getName(), "+++++++++++++++++++++++++++++++");
        Logger.info(this.getClass().getName(), "Current Thread Id: " + Thread.currentThread().getId());
        for (StackTraceElement i: Thread.currentThread().getStackTrace()) {
            Logger.info(this.getClass().getName(), i.toString());
        }
        Logger.info(this.getClass().getName(), "+++++++++++++++++++++++++++++++");
    }

    private void handle() throws GroundhogMessageException, IOException {
        GroundhogMessage sendMsg = null;
        GroundhogMessage recvMsg = connection.getMsg();

        Logger.verbose(this.getClass().getName(), recvMsg
                +", sequenceId:"+((GroundhogMessage) recvMsg).sequenceId
                +", functionId:"+((GroundhogMessage) recvMsg).functionId);
        
        this.lastRecvPkgTime = System.currentTimeMillis();
        
        if (recvMsg instanceof PushRequestMessage) {
            PushRequestMessage pushMsg = (PushRequestMessage) recvMsg;
            if (PushService.subscribeEnabled &&
                    recvMsg.messageType == GroundhogMessage.MESSAGE_TYPE_LATLNG)
            {
                PushService.subscribeInfo.lastRecvPkgTime = this.lastRecvPkgTime;
            }

            try {
                connection.sendAckForMessage(pushMsg);
            } catch (Exception e) {
                Logger.info(this.getClass().getName(), "Close connection due to "
                        + "sending ACK of message failed");
                this.close();
            } 
            broadcastPushMessage(pushMsg.pushMessage);
        } else if (recvMsg instanceof PingMessage) {
            PingMessage ping = (PingMessage) recvMsg;
            try {
                connection.sendAckForMessage(ping);
            } catch (Exception e) {
                Logger.info(this.getClass().getName(), "Close connection due to "
                        + "sending ACK of PING failed");
                this.close();
            }
        } else {
            if ((sendMsg = PushService.getWaitingResponse(recvMsg.sequenceId)) != null) {
                if (sendMsg instanceof PingMessage) {
                    if (recvMsg instanceof AcknowledgeMessage) {
                        PushService.removeWaitingResponse(recvMsg.sequenceId);
                    }
                } else if (sendMsg instanceof PushRequestMessage) {
                    if (recvMsg instanceof AcknowledgeMessage) {
                        PushService.removeWaitingResponse(recvMsg.sequenceId);
                    }
                } else if (sendMsg instanceof RpcRequestMessage) {
                    if (recvMsg instanceof RpcResponseMessage) {
                        PushService.removeWaitingResponse(recvMsg.sequenceId);
                        RpcResponseMessage rpcMsg = (RpcResponseMessage) recvMsg;
                        this.broadcastRpcResponse(recvMsg.sequenceId, rpcMsg.result);
                    }
                } else if (sendMsg instanceof SubscribeRequestMessage) {
                    if (recvMsg instanceof AcknowledgeMessage) {
                        Logger.info(this.getClass().getName(), "ACK of SubscribeRequestMessage, "
                                + "status: " + recvMsg.status);
                        if (recvMsg.status == GroundhogMessage.STATUS_SUCCESS) {
                            PushService.removeWaitingResponse(recvMsg.sequenceId);
                            PushService.retrySubscribeFlag = false;
                        } else if (recvMsg.status == GroundhogMessage.STATUS_EPERM) {
                            PushService.removeWaitingResponse(recvMsg.sequenceId);
                            PushService.subscribeEnabled = false;
                            PushService.retrySubscribeFlag = false;
                        }
                    }
                } else if (sendMsg instanceof UnsubscribeRequestMessage) {
                    if (recvMsg instanceof AcknowledgeMessage) {
                        Logger.info(this.getClass().getName(), "ACK of UnsubscribeRequestMessage, "
                                + "status: " + recvMsg.status);
                        PushService.removeWaitingResponse(recvMsg.sequenceId);
                        if (recvMsg.status != GroundhogMessage.STATUS_SUCCESS) {
                            Logger.info(this.getClass().getName(), "Close connection due to "
                                    + "status of UnsubscribeRequestMessage is not success");
                            this.close();
                        }
                    }
                }
            }
        }
    }
}
