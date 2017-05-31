package letv.jmeter.yidao;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class Holocancel extends AbstractJavaSamplerClient implements Serializable {
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
	String[] serviceCenter; 
	String order_id; 
	String return_min;
	String reason_id;
	String extension;
	String user_confirmed;
	String name;
	private static final long serialVersionUID = 10003L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {
		
		serviceCenter = context.getParameter("serviceCenter").split(",");
		
		return_min = context.getParameter("return_min");
		reason_id = context.getParameter("reason_id");
		extension = context.getParameter("extension");
		user_confirmed = context.getParameter("user_confirmed");
	//	order_id = context.getParameter("order_id");
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
		params.addArgument("order_id", "based on creatorder return value");
		params.addArgument("return_min", "1");
		params.addArgument("reason_id", "0");
		params.addArgument("extension", "{\"other_reason\":\"\"}");
		params.addArgument("user_confirmed", "0");
		return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
		
		order_id = context.getParameter("order_id");
		request.data = "";
		request.service_uri = String.format(
				"State/cancel?order_id=%s&return_min=%s&reason_id=%s&extension=%s&user_confirmed=%s",
				order_id, return_min, reason_id, extension, user_confirmed);

	results.setSamplerData("Data:\n" + request.service_uri);
		try {			
			results.sampleStart();
			String response = psf.call("order", request);
			results.setResponseData(response, "UTF-8");
		//	System.out.println( "cancel"+ response);
			//System.out.println( "cancel"+ request.service_uri);
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


