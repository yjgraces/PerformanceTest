package letv.jmeter.yidao;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class HoloCreateOrder extends AbstractJavaSamplerClient implements Serializable {
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
	String[] serviceCenter; 
	String user_id;
	String passenger_phone;
	String city;
	String car_type_id;
	String expect_end_latitude;
	String expect_end_longitude;
	String expect_start_latitude;
	String expect_start_longitude;
	String has_custom_decision;
	String is_need_manual_dispatch;
	String is_auto_dispatch;
	String create_order_longitude;
	String create_order_latitude;
	String dispatch_type;
	String flag;
	String name;
	String product_type_id;
	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {
		serviceCenter = context.getParameter("serviceCenter").split(",");
		user_id = context.getParameter("user_id");
		passenger_phone = context.getParameter("passenger_phone");
		city = context.getParameter("city");
		car_type_id = context.getParameter("car_type_id");
		expect_end_latitude = context.getParameter("expect_end_latitude");
		expect_end_longitude = context.getParameter("expect_end_longitude");
		expect_start_latitude = context.getParameter("expect_start_latitude");
		expect_start_longitude = context.getParameter("expect_start_longitude");
		has_custom_decision = context.getParameter("has_custom_decision");
		is_need_manual_dispatch = context.getParameter("is_need_manual_dispatch");
		is_auto_dispatch = context.getParameter("is_auto_dispatch");
		create_order_longitude = context.getParameter("create_order_longitude"); 
		create_order_latitude = context.getParameter("create_order_latitude");
		dispatch_type = context.getParameter("dispatch_type");
		flag = context.getParameter("flag");
		product_type_id=context.getParameter("product_type_id");
		
		name = context.getParameter(TestElement.NAME);
		
		try {
			psf = new PSFClient(serviceCenter);
			request = new PSFClient.PSFRPCRequestData();
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();
		params.addArgument("serviceCenter","10.0.11.71:5201,10.0.11.72:5201");
		params.addArgument("user_id", "13025137");
		params.addArgument("passenger_phone", "16809340982"); 
		params.addArgument("city", "bj");
		params.addArgument("car_type_id", "3");
		params.addArgument("expect_end_latitude", "36.9021");
		params.addArgument("expect_end_longitude", "100.1521");
		params.addArgument("expect_start_latitude", "36.9022");
		params.addArgument("expect_start_longitude", "100.1522");
		params.addArgument("has_custom_decision", "1");
		params.addArgument("is_need_manual_dispatch", "0");
		params.addArgument("is_auto_dispatch", "1");
		params.addArgument("flag", "2");
		params.addArgument("create_order_longitude", "36.9022");
		params.addArgument("create_order_latitude", "100.1522");
		params.addArgument("dispatch_type", "2");
		params.addArgument("product_type_id", "1");
		
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);

		request.data = "";
		request.service_uri = String.format(
				"state/createOrder?user_id=%s&corporate_id=0&passenger_phone=%s&passenger_name=testyace&passenger_number=1&city=%s&product_type_id=%s&fixed_product_id=0&car_type_id=%s&car_type_ids=3&source=20000001&expect_start_time=%s&in_coord_type=baidu&expect_end_latitude=%s&expect_end_longitude=%s&expect_start_latitude=%s&expect_start_longitude=%s&start_position=testaddr&start_address=testaddr&end_position=testaddr&end_address=testaddr&flight_number=0&is_asap=1&app_version=iWeidao/6.2.5 D/877035&media_id=1&sms=passenger&time_span=0&has_custom_decision=%s&is_need_manual_dispatch=%s&is_auto_dispatch=%s&estimate_price=0&device_id=0&corporate_dept_id=0&estimate_price=100.0&estimate_info=D123,T3700&flag=%s&create_order_longitude=%s&create_order_latitude=%s&ip=10.1.7.202&order_port=60428&dispatch_type=%s&time_length=1800",
				user_id, passenger_phone, city, product_type_id,car_type_id, (System.currentTimeMillis()/1000+120), expect_end_latitude, expect_end_longitude, expect_start_latitude, expect_start_longitude,has_custom_decision,is_need_manual_dispatch,is_auto_dispatch,flag,create_order_longitude, create_order_latitude, dispatch_type);

		results.setSamplerData("Data:\n" + request.service_uri);
		try { 
			results.sampleStart();
			String response = psf.call("order", request);
			results.setResponseData(response, "UTF-8");
			//System.out.println( "creatorder"+ request.service_uri);
			//System.out.println( "creatorder"+ response);            
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
