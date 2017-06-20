/*
 * LoadRunner Java script. (Build: _build_number_)
 * 
 * Script Description: 
 *                     
 */

import java.util.HashMap;
import java.util.Map;

import com.yidao.getSign;

import lrapi.*;
import lrapi.lr;
import lrapi.web;


public class Actions
{

	public String userApiHost="https://ycagent.yongche.com";
	public String time=(System.currentTimeMillis()/1000)+"";

	public int init() throws Throwable {

	    return 0;
	}//end of init



	public int action() throws Throwable {
	    web.set_sockets_option("SSL_VERSION","TLS");
	    Actions a=new Actions();

	    a.estimate();//����Ԥ��
	    a.creatOrder(); //��������
	    a.getStatus(); //��ȡ����״̬,�鿴�Ƿ���˾���ӵ�
	  //  lr.think_time(1);
	    a.cancelOrder(); //ȥ������

	  return 0;		
		
	}//end of action


	public int end() throws Throwable {
		return 0;
	}//end of end


	/*
	============��ȡ����Ԥ��==================
        */
	public int estimate(){

		Map<String,String> estimateMapParams=new HashMap<String,String>();
		estimateMapParams=getSign.getParameterFromFileAsMap("getestimatedcost.txt");
		
		estimateMapParams.put("start_time", time);
		estimateMapParams.put("nonce", time);	    
		estimateMapParams.put("start_latitude", "38.77<lat>");
		estimateMapParams.put("end_latitude", "38.77<lat>");		    
		estimateMapParams.put("start_longitude", "82.68<lng>");
		estimateMapParams.put("end_longitude", "82.68<lng>");	    	    
		estimateMapParams.put("passenger_phone","<phone>" );

			    
		String sign=getSign.getSignKey(estimateMapParams);
		//System.out.println("��ӡsign:==="+sign);
		String str=getSign.doGetParamsStr(estimateMapParams);
		//System.out.println("���������"+str);
		// 
		web.add_header("Authorization","Bearer "+"<token>");
		web.add_header ("User-Agent","aWeidao/7.2.2 (X900; Android 5.0.2)");
		web.add_header ("sign",sign);

		web.reg_find("Text=\"ret_code\":200", new String []{"SaveCount=getEstimatedCostSuccess", "LAST"}); 

		lr.start_transaction("����Ԥ��OrderEstimated");
		try{

		web.custom_request("yidao_orderEstimated",
			    "Method=GET",	    
			    new String[]{ 
			    "URL="+userApiHost+"/order/estimate?"+str,
			    "TargetFrame=",
			    "LAST"}); 
		} 

		catch (Exception e) {} 

		if (lr.eval_int("<getEstimatedCostSuccess>") == 0){
		    lr.end_transaction("����Ԥ��OrderEstimated", lr.FAIL);
		    lr.message("����Ԥ��ʧ��"); 
		}
		else {
		    lr.end_transaction("����Ԥ��OrderEstimated", lr.PASS); 
		}
	    return 0;
	}


	/*
	============��������=============
        */
	public int creatOrder(){
	     Map<String,String> creatOrderMapParams=new HashMap<String,String>();
	     creatOrderMapParams=getSign.getParameterFromFileAsMap("createorder.txt");
	     System.out.println(creatOrderMapParams.toString());
	     creatOrderMapParams.put("start_time", time);
	     creatOrderMapParams.put("nonce", time);			
	     creatOrderMapParams.put("start_lng","82.68<lng>");
	     creatOrderMapParams.put("start_lat","38.77<lat>");
	     creatOrderMapParams.put("end_lat","38.77<lat>");
	     creatOrderMapParams.put("end_lng","82.68<lng>");
	     creatOrderMapParams.put("order_lng","82.68<lng>");
	     creatOrderMapParams.put("order_lat","38.77<lat>");
	     creatOrderMapParams.put("passenger_phone","<phone>");
	     System.out.println(creatOrderMapParams.toString());

	    String sign=getSign.getSignKey(creatOrderMapParams);
	    //System.out.println("��ӡsign:==="+sign);
	    String str=getSign.doGetParamsStr(creatOrderMapParams);
	    //System.out.println("���������"+str);
	    
	    web.reg_save_param("service_order_id",
		 new String[]{
		"LB=\"order_id\":\"", 
		"RB=\",\"preparation",
		"Search=Body",
		"LAST"});
	    
	    web.add_header("Authorization","Bearer "+"<token>");
	    web.add_header ("User-Agent","aWeidao/7.2.2 (X900; Android 5.0.2)");
	    web.add_header ("sign",sign);

	web.reg_find("Text=order_id", new String []{"SaveCount=creatOrderSuccess", "LAST"});

	lr.start_transaction("����ϵͳ���߶���createSysOrder");

		try{

		web.custom_request("yidao_creatOrder",
			    "Method=POST",	    
			    new String[]{ 
			    "URL="+userApiHost+"/order",
			    "Body="+str,
			    "TargetFrame=",
			    "LAST"}); 
		} 
		catch (Exception e) {} 

		if (lr.eval_int("<creatOrderSuccess>") == 0){
		    lr.end_transaction("����ϵͳ���߶���createSysOrder", lr.FAIL);
		    lr.message("��������ʧ��"); 
		}
		else {
		    lr.end_transaction("����ϵͳ���߶���createSysOrder", lr.PASS); 
		  //  System.out.println("���������ɹ���id:"+"<service_order_id>");
		}
	    return 0;

	}

	/*
	===============ȡ������=================
        */

	public int cancelOrder(){

	    Map<String,String> cancelOrderMapParams=new HashMap<String,String>();

		
		//cancelOrderMapParams.put("order_id",service_order_id);
		cancelOrderMapParams.put("order_id","<service_order_id>");
                cancelOrderMapParams.put("nonce", time);
					    
		String sign=getSign.getSignKey(cancelOrderMapParams);
		//System.out.println("��ӡsign:==="+sign);
		String str=getSign.doGetParamsStr(cancelOrderMapParams);
		//System.out.println("���������"+str);
		// 
		web.add_header("Authorization","Bearer "+"<token>");
		web.add_header ("User-Agent","aWeidao/7.2.2 (X900; Android 5.0.2)");
		web.add_header ("sign",sign);

		web.reg_find("Text=\"ret_code\":200", new String []{"SaveCount=CancelOrderSuccess", "LAST"}); 

		lr.start_transaction("ȡ������cancelOrder");
		try{

		web.custom_request("yidao_cancelOrder",
			    "Method=POST",	    
			    new String[]{ 
			    "URL="+userApiHost+"/order/cancel",
			    "Body="+str,
			    "TargetFrame=",
			    "LAST"}); 
		} 

		catch (Exception e) {} 

		if (lr.eval_int("<CancelOrderSuccess>") == 0){
		    lr.end_transaction("ȡ������cancelOrder", lr.FAIL);
		    lr.message("ȡ������ʧ��:"+"<service_order_id>"); 
		}
		else {
		    lr.end_transaction("ȡ������cancelOrder", lr.PASS); 
		}
	    return 0;
	}


	/*
	==========��ȡ����״̬===============
	*/

	public int getStatus(){

	      int i=0;
    
	      Map<String,String> orderStatusMapParams=new HashMap<String,String>();
	      web.reg_save_param("orderStatus",
		 new String[]{
		"LB=\"status\":\"", 
		"RB=\",\"flag",
		"Search=Body",
		"LAST"});  
    
	     web.reg_find("Text=\"status\":\"4", new String []{"SaveCount=getOrderStatus", "LAST"});
    
	     orderStatusMapParams.put("order_id","<service_order_id>");
	    // orderStatusMapParams.put("order_id",service_order_id);
	     orderStatusMapParams.put("nonce", time);
	     String sign=getSign.getSignKey(orderStatusMapParams);
	     String str=getSign.doGetParamsStr(orderStatusMapParams);
    
	     web.add_header("Authorization","Bearer "+"<token>");
	     web.add_header ("User-Agent","aWeidao/7.2.2 (X900; Android 5.0.2)");
	     web.add_header ("sign",sign);

	    lr.think_time(1);
	    lr.start_transaction("��ȡ����״̬getOrderStatus");
	    try{

	    web.custom_request("yidao_getOrderStatus",
			"Method=GET",	    
			new String[]{ 
			"URL="+userApiHost+"/order/status?"+str,
			"TargetFrame=",
			"LAST"}); 
	    } 

	    catch (Exception e) {} 

		
		while (lr.eval_int("<getOrderStatus>") == 0&&i<3){
		    lr.think_time(1);
		    web.reg_find("Text=\"status\":\"4", new String []{"SaveCount=getOrderStatus", "LAST"});
		    orderStatusMapParams.put("order_id","<service_order_id>");
		  //   orderStatusMapParams.put("order_id",service_order_id);
		     orderStatusMapParams.put("nonce", time);
		    sign=getSign.getSignKey(orderStatusMapParams);
		    str=getSign.doGetParamsStr(orderStatusMapParams);

		    web.add_header("Authorization","Bearer "+"<token>");
		    web.add_header ("User-Agent","aWeidao/7.2.2 (X900; Android 5.0.2)");
		    web.add_header ("sign",sign);

		       try{
		       web.custom_request("yidao_getOrderStatus",
				   "Method=GET",	    
				   new String[]{ 
				   "URL="+userApiHost+"/order/status?"+str,
				   "TargetFrame=",
				   "LAST"}); 
		       } 
		       catch (Exception e) {} 
		    i++;
		}
		
		    lr.end_transaction("��ȡ����״̬getOrderStatus", lr.AUTO); 

		 //    Actions b=new Actions();
		 //    b.cancelOrder();

		

	    return 0;

	}


}
