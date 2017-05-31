package letv.jmeter.yidao;

import java.util.HashMap;
import java.util.Map;



import net.sf.json.JSONObject;

public class UserAppApiInter{
	
	public JSONObject jsonObj = null;
	public static String userApiHost="https://ycagent.yongche.com";
	public static String orderEstimate=userApiHost+"/order/estimate";
	public static String creatOrder= userApiHost+"/order";
	public static String orderStatus=userApiHost+"/order/status";
	public static String orderCancel=userApiHost+"/order/cancel";
	public static String acceptcar=userApiHost+"/order/acceptcar";
	public static String decisiondriver=userApiHost+"/order/decisiondriver";
	
	public String time=(System.currentTimeMillis()/1000)+"";
	static String alertmsg="";

/*
 * 接口
 */
	
	//接口名称：获取订单价格预估 /order/estimate
	public static String orderEstimate(Map<String,String> mapParams,String accessToken){
		String url = orderEstimate;					
		Map<String,String> headerParams = new HashMap<>();	
		mapParams.put("city", "hlbe");
		mapParams.put("car_type_id", "3");
		mapParams.put("time_length", "1800");
		mapParams.put("corp_id", "");
		mapParams.put("is_asap", "1");		
		mapParams.put("start_time", (System.currentTimeMillis()/1000)+"");
		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");
		
		String sign=getSign.getSignKey(mapParams);
	//	System.out.println("订单预估签名："+sign+"订单预估token"+accessToken);
	//	System.out.println("订单预估请求参数："+getSign.doGetParamsStr(mapParams));
		
		headerParams.put("sign", sign);	
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");

		Object rs = HttpUtils.getIntance().doSendGet(url,mapParams,headerParams);
		return rs.toString();
		
	}
	//接口名称： 创建订单 /order
	public static String creatOrder(Map<String,String> creatOrderMapParams,String accessToken){
		String url=creatOrder;			
		Map<String,String> headerParams = new HashMap<>();
			
	     creatOrderMapParams.put("start_time", (System.currentTimeMillis()/1000)+"");
	     creatOrderMapParams.put("nonce", (System.currentTimeMillis()/1000)+"");     	     
	     creatOrderMapParams.put("area_code", "");
	     creatOrderMapParams.put("corporate_id", "0");
	     creatOrderMapParams.put("from_pos", "testaddr");
	     creatOrderMapParams.put("to_pos", "testaddr");
	     creatOrderMapParams.put("start_address", "testaddr");
	     creatOrderMapParams.put("end_address", "testaddr");     
	     creatOrderMapParams.put("passenger_sms", "0");
	     creatOrderMapParams.put("passenger_name", "testyace");
	     creatOrderMapParams.put("in_coord_type", "baidu");
	     creatOrderMapParams.put("flight_number", "");
	     creatOrderMapParams.put("flight_data_id", "");
	     creatOrderMapParams.put("coupon_member", "");
	     creatOrderMapParams.put("favor_fm", "");     
	     creatOrderMapParams.put("favor_slow", "");
	     creatOrderMapParams.put("favor_chat", "");
	     creatOrderMapParams.put("favor_front_seat", "");
	     creatOrderMapParams.put("favor_air_condition", "");	     
	     creatOrderMapParams.put("favor_aromatherapy", "");
	     creatOrderMapParams.put("favor_emergency_light", "");
	     creatOrderMapParams.put("favor_no_call", "");
	     creatOrderMapParams.put("estimate_distance", "");     
	     creatOrderMapParams.put("estimate_time", "");
	     creatOrderMapParams.put("estimate_info", "");
	     creatOrderMapParams.put("driver_id", "");
	     creatOrderMapParams.put("ip", "");
	     creatOrderMapParams.put("order_port", "");
			
		String sign=getSign.getSignKey(creatOrderMapParams);
		
		headerParams.put("sign", sign);
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");		
		//System.out.println("token:"+accessToken+"创建订单请求参数==="+getSign.doGetParamsStr(creatOrderMapParams));
		
		Object rs = HttpUtils.getIntance().doSendPost(url,creatOrderMapParams,headerParams);		
		
		return rs.toString()+" token:\""+accessToken+"\"";		
	}
	
	//订单状态  /order/status
	public static String getOrderStatus1(Map<String,String> mapParams,String accessToken){
		String url=orderStatus;
		Map<String,String> headerParams = new HashMap<>();
		int order_Status=0;
		Object rs = null;
		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");	
		String sign=getSign.getSignKey(mapParams);	
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
		headerParams.put("sign", sign);
		try {
			rs = HttpUtils.getIntance().doSendGet(url,mapParams,headerParams);
		//	System.out.println("token:"+accessToken+"getstatus Response=====:"+rs.toString());
			int i=0;
			Thread.sleep(500);
			order_Status=JSONObject.fromObject(rs).getJSONObject("result").getInt("status");
			
			while(i<30&&(order_Status!=4)){	
			
				Thread.sleep(100);	
			//	System.out.println("getstatus请求参数++++++++："+mapParams.toString()+"token"+accessToken);
				rs = HttpUtils.getIntance().doSendGet(url,mapParams,headerParams);				
			//	System.out.println("token:"+accessToken+"getstatus Response++++++++:"+rs.toString());				
				order_Status=JSONObject.fromObject(rs).getJSONObject("result").getInt("status");				
			//	System.out.println(order_Status+"获取订单状态异常，正在重试:"+(i+1));
				i++;
			}	
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("获取订单状态异常==============正在重试:");
			}
						
		return rs.toString();	 
	}
	
	public static String getOrderStatus(Map<String,String> mapParams,String accessToken){
		String url=orderStatus;
		Map<String,String> headerParams = new HashMap<>();
		Object rs = null;
		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");
		String sign=getSign.getSignKey(mapParams);	
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
		headerParams.put("sign", sign);
		try {
			Thread.sleep(300);
		//	System.out.println("getstatus请求参数=======："+mapParams.toString()+"token:"+accessToken);
			rs = HttpUtils.getIntance().doSendGet(url,mapParams,headerParams);
		//	System.out.println("token:"+accessToken+"getstatus Response=====:"+rs.toString());				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("获取订单状态异常==============正在重试:");
			}
						
		return rs.toString();	 
	}
	//取消订单  /order/cancel
	public static String orderCancel(Map<String,String> mapParams,String accessToken){
		String url=orderCancel;		
		Map<String,String> headerParams = new HashMap<>();
		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");
		//mapParams.put("user_confirmed", "0");
		String sign=getSign.getSignKey(mapParams);	
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
		headerParams.put("sign", sign);
		
	//	System.out.println("cancel order请求参数=+=+=+=+=+==："+mapParams.toString()+"token"+accessToken);
		Object rs = HttpUtils.getIntance().doSendPost(url,mapParams,headerParams);
	//	System.out.println("token:"+accessToken+"cancel order Response:=+=+=+=+=+==："+rs.toString());
		return rs.toString();
	
	}
	
//	//获取接受订单的司机列表 /order/acceptcar
//	public static String acceptCar(String accessToken,String order_Id){
//		String url=acceptcar;
//		boolean acceptRS = false;
//		Map<String,String> mapParams = new HashMap<>();		
//		Map<String,String> headerParams = new HashMap<>();
//		JSONArray car_list = null;
//		Object rs=null;
//		mapParams.put("order_id", order_Id);
//		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");
//		 getSign getSign=new getSign();
//		String sign=getSign.getSignKey(mapParams);	
//		headerParams.put("Authorization", "Bearer "+accessToken);
//		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
//		headerParams.put("sign", sign);
//		
//		for(int i=0;i<10;i++){
//						
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			rs = HttpUtils.getIntance().doSendGet(url,mapParams,headerParams);	
//			alertmsg="获取接受订单的司机列表 /order/acceptcar接口response:"+rs.toString();
//		//	System.out.println(alertmsg);
//		//	jsonObj = JSONObject.toString(rs.toString());
//			jsonObj = JSONObject.fromObject(rs);
//			int ret_code=jsonObj.getInt("ret_code");
//			//System.out.println("ret_code:"+ret_code);
//					
//			if (ret_code==498){
//			//	System.out.println("获取司机列表失败,订单号为："+order_Id);
//			//	System.out.println("请求response:"+rs.toString());
//				break;
//			}else if(ret_code==200){
//				int car_num=jsonObj.getJSONObject("result").getJSONArray("car_list").size();
//			//	System.out.println("car_num"+car_num);
//				if(car_num>0){
//					car_list= jsonObj.getJSONObject("result").getJSONArray("car_list");
//			//		driver_Id=car_list.getJSONObject(0).getString("driver_id");
//					acceptRS=true;
//					break;
//				}
//			}
//
//		}
//					
//            return rs.toString();
//		
//	}
//	
//	// 选司机 /order/decisiondriver
//	public void decisionDriver(String accessToken,String order_Id,String driver_Id){
//		String url=decisiondriver;
//		Map<String,String> mapParams = new HashMap<>();		
//		Map<String,String> headerParams = new HashMap<>();
//
//		mapParams.put("order_id", order_Id);
//		mapParams.put("driver_id", driver_Id);
//		mapParams.put("nonce", (System.currentTimeMillis()/1000)+"");
//		mapParams.put("coupon_member_id", "0");
//		mapParams.put("third_party_coupon", "0");
//		getSign getSign=new getSign();
//		String sign=getSign.getSignKey(mapParams);	
//		headerParams.put("Authorization", "Bearer "+accessToken);
//		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
//		headerParams.put("sign", sign);
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//			Object rs = HttpUtils.getIntance().doSendPost(url,mapParams,headerParams);
//
//	
//	}

	
}
