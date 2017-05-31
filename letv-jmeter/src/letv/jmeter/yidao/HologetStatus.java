package letv.jmeter.yidao;

import java.io.Serializable;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.json.JSONObject;

public class HologetStatus extends AbstractJavaSamplerClient implements Serializable {
	private PSFClient psf = null;
	private PSFClient.PSFRPCRequestData request = null;
	String[] serviceCenter; 
	String order_id;
	String name;
	
	private static final long serialVersionUID = 10005L;
	
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
		params.addArgument("serviceCenter","10.0.11.71:5201,10.0.11.72:5201");
		params.addArgument("order_id", "based on creatorder return value");
			return params;
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMddHH-mmss-SSS");
		order_id = context.getParameter("order_id");

		
		request.data = "";
		request.service_uri = String.format(
				"order/getStatus?order_id=%s",
				order_id);

		results.setSamplerData("Data:\n" + request.service_uri);
		try {
			Thread.sleep(1000);
			results.sampleStart();
			String sysbgtime = sdf.format(date);
			String response = psf.call("order", request);
			String sysendtime = sdf.format(date);
		
		//	System.out.println( "begin time"+ sysbgtime);
		//	System.out.println( "getStatus"+ response);
		//	System.out.println( "getStatus-lenth..."+ response.length()); //
			
			
			
			JSONObject obj = JSONObject.fromObject(response);
			String statusresult = obj.getString("result");
			JSONObject status = JSONObject.fromObject(statusresult);
			
		  //  System.out.println("status++++++++++++++:" + status.getString("status"));
				
			
			String len = status.getString("status");
			results.setResponseData(response, "UTF-8");
			int i =0;
			String res="";
			
			while((!len.equals("4")) && i < 50){
				Thread.sleep(500);
				res = psf.call("order", request);
			//	System.out.println( "getStatus"+ res);			
				JSONObject obj1 = JSONObject.fromObject(res);
				String statusresult1 = obj1.getString("result");
				JSONObject status1 = JSONObject.fromObject(statusresult1);
				System.out.println("status is wrong ======:" + status1.getString("status")+";"+"i===" +i);
				System.out.println( "getStatus"+ request.service_uri);
				len = status1.getString("status"); 
				results.setResponseData(res, "UTF-8");
			    i++;
			    }  
			
				
			//    System.out.println( "end time"+ sysendtime);
			    
				
		//	System.out.println( "getStatus"+ request.service_uri); 
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
