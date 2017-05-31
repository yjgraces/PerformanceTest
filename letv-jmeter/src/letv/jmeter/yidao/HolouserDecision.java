package letv.jmeter.yidao;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class HolouserDecision extends AbstractJavaSamplerClient implements Serializable {
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
	String[] serviceCenter;
	String service_order_id;
	String driver_id;
	String coupon_member_id;

	String name;
	private static final long serialVersionUID = 10004L;

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
		params.addArgument("service_order_id", "based on creatorder return value");
		params.addArgument("driver_id", "111");
		params.addArgument("coupon_member_id", "0");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);

		service_order_id = context.getParameter("service_order_id");
		driver_id = context.getParameter("driver_id");
		coupon_member_id = context.getParameter("coupon_member_id");

		request.data = "";
		request.service_uri = String.format("Dispatch/userDecision?service_order_id=%s&driver_id=%s&coupon_member_id=%s", service_order_id, driver_id, coupon_member_id);

		results.setSamplerData("Data:\n" + request.service_uri);
		try {
			Thread.sleep(1000);
			results.sampleStart();
			String response = psf.call("dispatch", request);
			results.setResponseData(response, "UTF-8");
			// System.out.println( "userDecision:"+ request.service_uri);
			//System.out.println("orderid:" + service_order_id + " driverid:" + driver_id + " userDecision:" + response);
			// System.out.println( "userDecision"+ request.service_uri);
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
