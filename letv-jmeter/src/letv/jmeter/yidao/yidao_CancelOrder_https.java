package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class yidao_CancelOrder_https extends AbstractJavaSamplerClient implements Serializable {
	
	
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
		params.addArgument("accessToken", "c9da39bc60d07f4c3217ec5723c83b8ab69e958c");
		params.addArgument("order_id", "depends from creatorder");
		
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
	
		String order_id=context.getParameter("order_id");
		String accessToken=context.getParameter("accessToken");		
		String name = context.getParameter(TestElement.NAME);
		
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
			
		 Map<String,String> cancelMapParams=new HashMap<String,String>();
		cancelMapParams.put("order_id",order_id);
		results.setSamplerData("cancelOrder Data:\n cancelOrder"+getSign.doGetParamsStr(cancelMapParams)+"\n token:"+accessToken);
				
		try { 
			results.sampleStart();
			String response=UserAppApiInter.orderCancel(cancelMapParams, accessToken);	
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
