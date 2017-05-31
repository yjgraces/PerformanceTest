package com.yongche.component.groundhog.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.message.AcknowledgeMessage;
import com.yongche.component.groundhog.message.GroundhogMessage;
import com.yongche.component.groundhog.message.GroundhogMessageException;
import com.yongche.component.groundhog.message.PingMessage;
import com.yongche.component.groundhog.message.PushRequestMessage;
import com.yongche.component.groundhog.push.MCConnection;

import common.PSFClient;

public class DriverLogin implements Runnable {

	public static HttpUtil httpUtils;
	private MCConnection persistentConnection;
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
    private Map<String, String> parms =new HashMap<String,String>(); 
    private Map<String, String> headerMap=new HashMap<String,String>();

	DriverInfo driverInfo;

	public DriverLogin(DriverInfo driverInfo, String[] hosts) {
		this.driverInfo = driverInfo;
		ArrayList<String> managerHosts = new ArrayList<String>();
		if (hosts != null) {
			for (String host : hosts) {
				if (!managerHosts.contains(host)) {
					managerHosts.add(host);
				}
			}
		}

		/*
		 * for (String host : CommonConfig.MANAGER_HOST) { managerHosts.add(host
		 * + ":" + CommonConfig.MANAGER_PORT); }
		 */

		JSONArray arr = new JSONArray(managerHosts);
		String Hosts = arr.toString();

		// 没有传service 和 strage。 strage的内容直接用setSessionInfo传了
		// persistentConnection = new McPersistentConnection(null, null);
		persistentConnection = new MCConnection(null);
		persistentConnection.setSessionInfo(Hosts, System.currentTimeMillis(), driverInfo.getUserType(), driverInfo.getUserid(), driverInfo.getDeviceId(), driverInfo.getToken());
		// 测试环境
		// String[] serviceCenter = { "10.0.11.71:5201", "10.0.11.72:5201" };
		// 线上环境
		String[] serviceCenter = { "172.17.0.77:5201", "172.17.0.78:5201" };
		try {
			psf = new PSFClient(serviceCenter);
			request = new PSFClient.PSFRPCRequestData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void LoginMC() {
		
		try {
			persistentConnection.retrieveWorkerInfo();
			persistentConnection.makeWorkerConnection();
			// 保持心跳，50秒发一次
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							// System.out.println("send ping message " +
							// Thread.currentThread().getId());
							persistentConnection.connection.ping(60);
							Thread.sleep(50000);
						} catch (ClientException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				}
			}).start();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		loopRec();
	}

	public void Close() {
		persistentConnection.close();
	}

	/**
	 * 收消息，不停
	 */
	public void loopRec() {
		//System.out.println("===============================loopRec");
		WorkerClient wc = persistentConnection.connection;
		while (wc.isConnected()) {
			// System.out.println("connected");
			try {
				GroundhogMessage recvMsg = wc.getMsg();
				// System.out.println("消息类型：" + recvMsg.toString());
				if (recvMsg instanceof PushRequestMessage) {
					// System.out.println("PushRequestMessage coming:");
					PushRequestMessage pushMsg = (PushRequestMessage) recvMsg;
					wc.sendAckForMessage(pushMsg);
					if (pushMsg.messageType == GroundhogMessage.MESSAGE_TYPE_DISPATCH) {
						// System.out.println("派单信息：\r\n" +
						// pushMsg.pushMessage);
						JSONObject jobj = new JSONObject(pushMsg.pushMessage);
						
						final int round = jobj.getJSONObject("params").getJSONObject("order").getInt("round");
						final String orderNo = jobj.getJSONObject("params").getJSONObject("order").getString("order_id");
						final int batch = jobj.getJSONObject("params").getJSONObject("order").getInt("batch");
						//System.out.println("pushMessage:"+jobj.toString());
						// Random r = new Random();
						// if (r.nextInt(5) == 0) {
						if (round == 1 && (batch == 1)) {

							// new Thread(new Runnable() {
							// public void run() {
							System.out.println("===============================grabOrder");
							grabOrder(orderNo, String.valueOf(batch), String.valueOf(round));
							// }
							// }).start();
						}
						// }
					}
				} else if (recvMsg instanceof PingMessage) {
					// 这个是测试心跳是否回包的
					// System.out.println("ping message");
				} else if (recvMsg instanceof AcknowledgeMessage) {
					wc.sendAckForMessage(recvMsg);
					// System.out.println("AcknowledgeMessage ack");
				}

			} catch (GroundhogMessageException e) {
				wc.close();
				System.out.println(driverInfo.getUserid());
				e.printStackTrace();
			} catch (IOException e) {
				wc.close();
				System.out.println(driverInfo.getUserid());
				e.printStackTrace();
			} catch (JSONException e) {
				wc.close();
				System.out.println(driverInfo.getUserid());
				e.printStackTrace();				
			} catch (MessageException e) {
				wc.close();
				System.out.println(driverInfo.getUserid());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		LoginMC();
	}

	/**
	 * 司机抢单
	 * 
	 * @param orderNo
	 * @param batch
	 * @param round
	 */
	// psf方式接单
	private void grabOrder2(String orderNo, String batch, String round) {
		System.out.println("=========司机接单.订单id：" + orderNo + " 司机driverId" +driverInfo.getUserid());
		
		request.data = "";
		String params = "order_id=" + orderNo + "&in_coord_type=baidu&car_id=" + driverInfo.getCarId() + "&latitude=36.902685&longitude=100.152054&drive_time=1800&batch=" + batch + "&round=" + round + "&is_auto=0&distance=1000";
		request.service_uri = "/Dispatch/driverResponse?" + params;
		String response = "";
		long start = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		String dateStr = sdf.format(start);
		System.out.println("=========司机接单.订单id：" + orderNo + " 司机driverId" + driverInfo.getUserid() + " 时间:" + dateStr);
		try {
			response = psf.call("dispatch", request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		double resTime = Double.valueOf(end - start);
//		 System.out.println("抢单结果：" + response);// 打印出来测试方法，测试时注掉
//		 System.out.println("抢单请求参数："+params);
		DriverModel.transactions.getAndIncrement();
		DriverModel.addResTime(resTime);
	}
	
		
	//司机抢单 http方式接单
		private void grabOrder(String orderNo, String batch, String round) {
			httpUtils = HttpUtil.getIntance();	
			String url="http://driver-api.yongche.com/order/operateOrder";
			String oauth_Nonce=String.valueOf(System.currentTimeMillis()/1000+1000)+"";
			String oauth_Timestamp=String.valueOf(System.currentTimeMillis()/1000);
			String Time=String.valueOf(System.currentTimeMillis()/1000);
	      
	       parms.put("access_token", driverInfo.getAccess_token());  //变量         
	       parms.put("imei", driverInfo.getImei()); //是否需要和数据库一致   
	       parms.put("order_id",orderNo);
	       parms.put("round",round);
	       parms.put("batch",batch);        
	       parms.put("time",Time);  //如何实时获取
	       parms.put("is_auto","0");
	       parms.put("in_coord_type","baidu");
	       parms.put("method","accept");              
	       parms.put("drive_time", "1800");
	      // parms.put("longitude","82.681054");  //性能测试坐标
	       parms.put("longitude","100.152154"); //自动化监控坐标
	       parms.put("highway_amount","0.0");
	       parms.put("latitude","36.902185"); //自动化监控坐标
	    // parms.put("latitude","38.772685"); //性能测试坐标
	       parms.put("driver_add_price","0");
	       parms.put("distance","1000");
	       parms.put("provider","network");
	     //  parms.put("version", "217");
	       parms.put("version", "100");
	       parms.put("x_auth_mode", "client_auth");
	       parms.put("is_gzip", "true");
	       parms.put("device_type", "1");  
	       parms.put("os_name", "Huawei-HUAWEI C8817E");
	       parms.put("os_version", "6.0");
	       parms.put("city", "呼伦贝尔");
	       parms.put("channel_source", "");

	              
	      // String userAgent="aCarMaster/6.0/100/"+driverInfo.getImei()+"(Huawei-HUAWEI C8817E; Android 6.0)";	       
	      // Authorization  OAuth oauth_nonce=1481877006,oauth_signature=5sARLGoVkNAPhh5wq1Hl95crWIk,oauth_token=edb068702c5d0f16e493e1233e1d0808058510433,oauth_consumer_key=4821726c1947cdf3eebacade98173939,oauth_token_secret=0b8e647992367501d777e67eb0e09bbf,oauth_signature_method=PLAINTEXT,oauth_timestamp=1481876006,oauth_version=1.0
	      // String Authorization="OAuth oauth_nonce="+oauth_Nonce+",oauth_signature=5sARLGoVkNAPhh5wq1Hl95crWIk,oauth_token="+driverInfo.getOauth_token()+",oauth_consumer_key=4821726c1947cdf3eebacade98173939,oauth_token_secret="+driverInfo.getOauth_token_secret()+",oauth_signature_method=PLAINTEXT,oauth_timestamp="+oauth_Timestamp+",oauth_version=1.0";
	       headerMap.put("Content-Type","application/x-www-form-urlencoded");  
	       headerMap.put("Accept-Encoding", "gzip"); 
	       headerMap.put("User-Agent","aCarMaster/6.0/100/"+driverInfo.getImei()+"(Huawei-HUAWEI C8817E; Android 6.0)");
	       headerMap.put("Authorization","OAuth oauth_nonce="+oauth_Nonce+",oauth_signature=5sARLGoVkNAPhh5wq1Hl95crWIk,oauth_token="+driverInfo.getOauth_token()+",oauth_consumer_key=4821726c1947cdf3eebacade98173939,oauth_token_secret="+driverInfo.getOauth_token_secret()+",oauth_signature_method=PLAINTEXT,oauth_timestamp="+oauth_Timestamp+",oauth_version=1.0");
	       
	   //    System.out.println("Authorization====="+"OAuth oauth_nonce="+oauth_Nonce+",oauth_signature=5sARLGoVkNAPhh5wq1Hl95crWIk,oauth_token="+driverInfo.getOauth_token()+",oauth_consumer_key=4821726c1947cdf3eebacade98173939,oauth_token_secret="+driverInfo.getOauth_token_secret()+",oauth_signature_method=PLAINTEXT,oauth_timestamp="+oauth_Timestamp+",oauth_version=1.0");
	       

			long start = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
			String dateStr = sdf.format(start);
			System.out.println("=======司机收到接单消息.订单id：" + orderNo + " 司机driverId" + driverInfo.getUserid()+ "设备号：" + driverInfo.getDeviceId() +" token："+driverInfo.getAccess_token()+" 时间:" + dateStr);
			try {			
				Object obj = httpUtils.doSendPostOAuthcf(url,parms,headerMap);
				
				if(obj!=null){
					String response=obj.toString();
					System.out.println(driverInfo.getUserid()+"抢单结果："+response);// 打印出来测试方法，测试时注掉
				}else{
					System.out.println(driverInfo.getUserid()+"=========抢单请求失败===========");					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			double resTime = Double.valueOf(end - start);
			DriverModel.transactions.getAndIncrement();
			DriverModel.addResTime(resTime);
		}
	

}
