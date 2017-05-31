package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

import net.sf.json.JSONObject;

public class yidao_GetStatus_https_temp extends AbstractJavaSamplerClient implements Serializable {

	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public  void setupTest(JavaSamplerContext context) {
		
	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();	
		params.addArgument("accessToken", "c9da39bc60d07f4c3217ec5723c83b8ab69e958c");
		params.addArgument("order_id", "depends from creatorder");
		params.addArgument("timeWait", "1000");
	
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		String order_id=context.getParameter("order_id");
		String name= context.getParameter(TestElement.NAME);
		String accessToken=context.getParameter("accessToken");
		String timeWait=context.getParameter("timeWait");

		
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
			
		Map<String,String> getStatusMapParams=new HashMap<String,String>();
		
		getStatusMapParams.put("order_id",order_id);
		results.setSamplerData("getStatus Data:\n getStatus:"+getSign.doGetParamsStr(getStatusMapParams)+"\n token:"+accessToken);
		
		try { 
			Thread.sleep(Integer.parseInt(timeWait));
			results.sampleStart();	
			
			String response=UserAppApiInter.getOrderStatus1(getStatusMapParams,accessToken);
											
			results.setResponseData(response, "UTF-8"); 
									
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
