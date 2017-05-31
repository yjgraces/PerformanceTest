package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class yidao_activityp extends AbstractJavaSamplerClient implements Serializable {
	
	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {
//		String order_id=context.getParameter("order_id");
//		String accessToken=context.getParameter("accessToken");		
//		String name = context.getParameter(TestElement.NAME);		
	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();	
		params.addArgument("url", "/activity/findActivityRewards?cityCode=sz&driverId=50062620");
		params.addArgument("hosts", "10.0.11.71:5201,10.0.11.72:5201");
		params.addArgument("activity", "activity");
		params.addArgument("version", "1.0");
		params.addArgument("size","3");
		
//    	PsfClientInstance p=new PsfClientInstance();
//        String[] hosts = {"10.0.11.71:5201","10.0.11.72:5201"};
//        String url="/activity/findActivityRewards?cityCode=sz&driverId=50062620";
//        String rs1=p.getInstance(url,"activity", "1.0", hosts);
		
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
	
		String url=context.getParameter("url");
		String activity=context.getParameter("activity");
		String version=context.getParameter("version");
		String[] hosts=context.getParameter("hosts").split(",");
		int size=Integer.parseInt(context.getParameter("size"));
		String name = context.getParameter(TestElement.NAME);
		PsfClientInstance p=new PsfClientInstance();
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
			
		results.setSamplerData("cancelOrder Data:\n cancelOrder"+url);
				
		try { 
			results.sampleStart();
		//	String response=UserAppApiInter.orderCancel(cancelMapParams, accessToken);	
			String response=p.getInstance(url,activity, version, hosts,size);
			results.setResponseData(response, "UTF-8");
		//	System.out.println("response:===="+response);
            
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
