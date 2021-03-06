package letv.jmeter.yidao;

public class PsfTest {
	private static PSFClient psf = null;
	private static PSFClient.PSFRPCRequestData request = null;
	static String[] serviceCenter={"10.0.11.71:5201", "10.0.11.72:5201"};

	public static void main(String[] args) {
		request=new PSFClient.PSFRPCRequestData();
		request.data = "";
		request.service_uri="state/createOrder?user_id=13025137&corporate_id=0&passenger_phone=16809340982&passenger_name=王芳&passenger_number=1&city=bj&product_type_id=1&fixed_product_id=0&car_type_id=3&car_type_ids=3&source=20000001&expect_start_time=1474424255085&in_coord_type=baidu&expect_end_latitude=36.9021&expect_end_longitude=100.1521&expect_start_latitude=36.9022&expect_start_longitude=100.1522&start_position=testaddr&start_address=testaddr&end_position=testaddr&end_address=testaddr&flight_number=0&is_asap=1&app_version=iWeidao/6.2.5 D/877035&media_id=1&sms=passenger&time_span=0&has_custom_decision=1&is_need_manual_dispatch=0&is_auto_dispatch=1&estimate_price=0&device_id=0&corporate_dept_id=0&estimate_price=100.0&estimate_info=D123,T3700&flag=2&create_order_longitude=36.9022&create_order_latitude=36.9022&ip=10.1.7.202&order_port=60428&dispatch_type=2&time_length=1800";
	
		try {
			psf=new PSFClient(serviceCenter);
			String res=psf.call("order", request);
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
