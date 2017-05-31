package letv.jmeter.yidao;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class HologetAcceptCars extends AbstractJavaSamplerClient implements Serializable {
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
	String[] serviceCenter;
	String order_id;
	String out_coord_type; // baidu,mars
	String filter_driver_ids; // 
	String count; //
	String name;
	private static final long serialVersionUID = 10002L;

	@Override
	public void setupTest(JavaSamplerContext context) {
		serviceCenter = context.getParameter("serviceCenter").split(",");
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
		params.addArgument("serviceCenter", "10.0.11.71:5201,10.0.11.72:5201");
		params.addArgument("order_id", "based on creatorder return value");
		params.addArgument("out_coord_type", "baidu");
		params.addArgument("filter_driver_ids", "0");
		params.addArgument("count", "5");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMddHH-mmss-SSS");
		order_id = context.getParameter("order_id");
		out_coord_type = context.getParameter("out_coord_type");
		filter_driver_ids = context.getParameter("filter_driver_ids");
		count = context.getParameter("count");

		request.data = "";
		request.service_uri = String.format("Dispatch/getAcceptCars?order_id=%s&out_coord_type=%s&filter_driver_ids=%s&count=%s", order_id, out_coord_type, filter_driver_ids, count);

		results.setSamplerData("Data:\n" + request.service_uri);
		try {
			Thread.sleep(1000);
			results.sampleStart();
			String sysbgtime = sdf.format(date);
			String response = psf.call("dispatch", request);
			String sysendtime = sdf.format(date);

			// System.out.println( "begin time"+ sysbgtime);
			// System.out.println( "getAccpetCars-lenth..."+ response.length());

			int len = response.length();
			int i = 0;
			while (len < 2000 && i < 30) {
				Thread.sleep(300);
				response = psf.call("dispatch", request);
				// System.out.println(System.currentTimeMillis());
				// System.out.println("id:" + order_id + " getAccpetCars:" +
				// res);
				// System.out.println( "getAccpetCars"+ res);
				// System.out.println( "getAccpetCars-lenth..."+ res.length());
				// System.out.println( "getAccpetCars"+ request.service_uri);
				len = response.length();
				i++;
			}
			
			if (len > 780) {
				JSONObject obj = new JSONObject(response);
				JSONArray array = obj.getJSONArray("car_list");
				Random r = new Random();
				String driverSelected = array.getJSONObject(r.nextInt(array.length())).getString("driver_id");
				//System.out.println("driver selected is:" + driverSelected);
				//System.out.println("response is:"+response);
		        JMeterContext jmctx = JMeterContextService.getContext();
		        JMeterVariables vars = jmctx.getVariables();
		        vars.put("driverselected", driverSelected);
				results.setResponseData(response, "UTF-8");
				results.setResponseCodeOK();
				results.setSuccessful(true);
			}else{
				results.setResponseData("", "UTF-8");
				results.setResponseCode("4001");//4001表示没有找到任何接单司机
				results.setSuccessful(false);
			}


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
