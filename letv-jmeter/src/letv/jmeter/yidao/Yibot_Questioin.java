package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class Yibot_Questioin extends AbstractJavaSamplerClient implements Serializable {
	
	String question;
	String name;

	
	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {

		question=context.getParameter("question");	
		name = context.getParameter(TestElement.NAME);

	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();		
		params.addArgument("question", "我要打车");
		return params;  
	}

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
	
		results.setSamplerData("Data:\n" + question);
		System.out.println("request data:"+question);
		
		
		try { 
			results.sampleStart();
			Yibot_test y=new Yibot_test();	
			String response=y.yibot_request(question);			
			results.setResponseData(response, "UTF-8");
			System.out.println("response:===="+response);
            
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
