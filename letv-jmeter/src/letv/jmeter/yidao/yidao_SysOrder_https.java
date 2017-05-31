package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;


public class yidao_SysOrder_https extends AbstractJavaSamplerClient implements Serializable {
	String product_type_id;
	String name;
	String accessToken;	
	String start_lng;
	String start_lat;
	String end_lng;
	String end_lat;
	String order_lng;
	String order_lat;
	String passenger_phone;	     
	String city;
	String is_asap;	     
	String has_custom_decision;
	String is_need_manual_dispatch;
	String is_support_system_decision;	     
	String source;
	String car_type_id;

	
	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {

		product_type_id=context.getParameter("product_type_id");
		accessToken=context.getParameter("accessToken");
		start_lng=context.getParameter("start_lng");
		start_lat=context.getParameter("start_lat");
		end_lng=context.getParameter("end_lng");
		end_lat=context.getParameter("end_lat");
		order_lng=context.getParameter("order_lng");
		order_lat=context.getParameter("order_lat");
		passenger_phone=context.getParameter("passenger_phone");     
		city=context.getParameter("city");
		is_asap=context.getParameter("is_asap");     
		has_custom_decision=context.getParameter("has_custom_decision");
		is_need_manual_dispatch=context.getParameter("is_need_manual_dispatch");
		is_support_system_decision=context.getParameter("is_support_system_decision");	     
		source=context.getParameter("source");
		car_type_id=context.getParameter("car_type_id");
			
		name = context.getParameter(TestElement.NAME);

	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();	
		params.addArgument("accessToken", "c9da39bc60d07f4c3217ec5723c83b8ab69e958c");
		params.addArgument("passenger_phone","13391951392");	     
		params.addArgument("product_type_id","1");
		params.addArgument("city","hlbe");
		params.addArgument("is_asap","1");     
		params.addArgument("has_custom_decision","0");
		params.addArgument("is_need_manual_dispatch","0");
		params.addArgument("is_support_system_decision","1");	     
		params.addArgument("car_type_id","3");
		params.addArgument("source","20000001");
		params.addArgument("start_lng","82.68111");
		params.addArgument("start_lat","38.77111");
		params.addArgument("end_lat","38.77222");
		params.addArgument("end_lng","82.68222");
		params.addArgument("order_lng","82.68333");
		params.addArgument("order_lat","38.77333");
		
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
			
		Map<String,String> creatOrderMapParams=new HashMap<String,String>();
		
	     creatOrderMapParams.put("passenger_phone",passenger_phone);	     
	     creatOrderMapParams.put("product_type_id",product_type_id);
	     creatOrderMapParams.put("city",city);
	     creatOrderMapParams.put("is_asap",is_asap);     
	     creatOrderMapParams.put("has_custom_decision",has_custom_decision);
	     creatOrderMapParams.put("is_need_manual_dispatch",is_need_manual_dispatch);
	     creatOrderMapParams.put("is_support_system_decision",is_support_system_decision);	     
	     creatOrderMapParams.put("car_type_id",car_type_id);
	     creatOrderMapParams.put("source",source);
	     creatOrderMapParams.put("start_lng",start_lng);
	     creatOrderMapParams.put("start_lat",start_lat);
	     creatOrderMapParams.put("end_lat",end_lat);
	     creatOrderMapParams.put("end_lng",end_lng);
	     creatOrderMapParams.put("order_lng",order_lng);
	     creatOrderMapParams.put("order_lat",order_lat);
		results.setSamplerData("Data:\n creatOrder:"+getSign.doGetParamsStr(creatOrderMapParams)+"\n Token:"+accessToken);
	//	System.out.println("request data:"+question);
		
		
		try { 
			results.sampleStart();
			String response=UserAppApiInter.creatOrder(creatOrderMapParams, accessToken);	
			results.setResponseData(response, "UTF-8");
		//	System.out.println("token:"+accessToken+"创建订单response:===="+response);
            
			results.setResponseCodeOK();    
			results.setSuccessful(true);
		} catch (Exception e) {
			e.printStackTrace();
			results.setSuccessful(true);
			results.setResponseData(e.toString(), "UTF-8");	
		} finally {
			results.sampleEnd();
		}
		return results;
	}

}
