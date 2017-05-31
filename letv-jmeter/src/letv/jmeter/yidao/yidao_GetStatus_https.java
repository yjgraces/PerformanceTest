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

public class yidao_GetStatus_https extends AbstractJavaSamplerClient implements Serializable {
	
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
			System.out.println("jmeter请求参数orderid===:"+order_id);
			results.sampleStart();	
			String response=UserAppApiInter.getOrderStatus(getStatusMapParams, accessToken);
			String len=JSONObject.fromObject(response).getJSONObject("result").getString("status");
											
			results.setResponseData(response, "UTF-8"); 
			int i =0;
			String res="";
			
			while((!len.equals("4")) && i < 50){
				Thread.sleep(100);
				res = UserAppApiInter.getOrderStatus(getStatusMapParams, accessToken);
			//	System.out.println( "getStatus"+ res);			
				JSONObject obj1 = JSONObject.fromObject(res);
				String statusresult1 = obj1.getString("result");
				JSONObject status1 = JSONObject.fromObject(statusresult1);
				System.out.println("status is wrong ======:" + status1.getString("status")+";"+"i===" +i);
				
				len = status1.getString("status"); 
				results.setResponseData(res, "UTF-8");
			    i++;
			    }  
									
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
