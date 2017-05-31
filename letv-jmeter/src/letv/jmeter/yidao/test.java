package letv.jmeter.yidao;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

public class test {

	public void  test(){
		
		Map<String,String> creatOrderMapParams=new HashMap<String,String>();
		Map<String,String> headerParams = new HashMap<>();
		String accessToken="67e4e6267107158c22b894b04db45a2a2c82f617";
		
		
		String url="https://ycagent.yongche.com/order";

		
	//	creatOrderMapParams.put("start_time", (System.currentTimeMillis()/1000)+"");
	//	creatOrderMapParams.put("nonce", (System.currentTimeMillis()/1000)+""); 
		creatOrderMapParams.put("start_time", "1490336366");
		creatOrderMapParams.put("nonce", "1490336366"); 
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
		creatOrderMapParams.put("passenger_phone","16801234527");	     
		creatOrderMapParams.put("product_type_id","1");
		creatOrderMapParams.put("city","hlbe");
		creatOrderMapParams.put("is_asap","1");     
		creatOrderMapParams.put("has_custom_decision","0");
		creatOrderMapParams.put("is_need_manual_dispatch","0");
		creatOrderMapParams.put("is_support_system_decision","1");	     
		creatOrderMapParams.put("car_type_id","3");
		creatOrderMapParams.put("source","20000001");
		creatOrderMapParams.put("start_lng","82.68111");
		creatOrderMapParams.put("start_lat","38.77111");
		creatOrderMapParams.put("end_lat","38.77222");
		creatOrderMapParams.put("end_lng","82.68222");
		creatOrderMapParams.put("order_lng","82.68333");
		creatOrderMapParams.put("order_lat","38.77333");
		
		String sign=getSign.getSignKey(creatOrderMapParams);
		System.out.println("sign:===="+sign);
		headerParams.put("sign", sign);
		headerParams.put("Authorization", "Bearer "+accessToken);
		headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
		Object rs = HttpUtils.getIntance().doSendPost(url,creatOrderMapParams,headerParams);
	System.out.println(rs.toString());
	}
	
	public static void main(String[] args) {
		test t=new test();
		t.test();
		
	}
}
