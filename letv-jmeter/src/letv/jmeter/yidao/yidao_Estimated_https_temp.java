package letv.jmeter.yidao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

public class yidao_Estimated_https_temp extends AbstractJavaSamplerClient implements Serializable {
//	public Map<String,String> estimateMapParams=null;
//	String product_type_id=null;
//	String name=null;
//	String start_latitude=null;
//	String start_longitude=null;
//	String end_latitude=null;
//	String end_longitude=null;
//	String passenger_phone=null;
//	String in_coord_type=null;
//	String accessToken=null;

	
	private static final long serialVersionUID = 10001L;
	
	@Override
	public void setupTest(JavaSamplerContext context) {

//		product_type_id=context.getParameter("product_type_id");
//		start_latitude=context.getParameter("start_latitude");
//		start_longitude=context.getParameter("start_longitude");
//		end_latitude=context.getParameter("end_latitude");
//		end_longitude=context.getParameter("end_longitude");
//		passenger_phone=context.getParameter("passenger_phone");
//		in_coord_type=context.getParameter("in_coord_type");
//		accessToken=context.getParameter("accessToken");
//		
//		name = context.getParameter(TestElement.NAME);

	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments params = new Arguments();	
		params.addArgument("accessToken", "c9da39bc60d07f4c3217ec5723c83b8ab69e958c");
		params.addArgument("product_type_id", "1");
		params.addArgument("start_latitude", "38.77211");
		params.addArgument("start_longitude", "82.68211");
		params.addArgument("end_latitude", "38.77222");		
		params.addArgument("end_longitude", "82.68222");
		params.addArgument("passenger_phone", "16801234527");
		params.addArgument("in_coord_type", "baidu");
			
		return params;  
	}

	@Override
	public  SampleResult runTest(JavaSamplerContext context) {
		String product_type_id=context.getParameter("product_type_id");
		String start_latitude=context.getParameter("start_latitude");
		String start_longitude=context.getParameter("start_longitude");
		String end_latitude=context.getParameter("end_latitude");
		String end_longitude=context.getParameter("end_longitude");
		String passenger_phone=context.getParameter("passenger_phone");
		String in_coord_type=context.getParameter("in_coord_type");
		String accessToken=context.getParameter("accessToken");
		String name = context.getParameter(TestElement.NAME);
				
		SampleResult results = new SampleResult();
		results.setSampleLabel(name);
		String url = "https://ycagent.yongche.com/order/estimate";		
		Map<String,String> estimateMapParams=new HashMap<String,String>();
		estimateMapParams.put("product_type_id",product_type_id);
		estimateMapParams.put("start_latitude", start_latitude);
		estimateMapParams.put("start_longitude", start_longitude);
		estimateMapParams.put("end_latitude", end_latitude);		
		estimateMapParams.put("end_longitude", end_longitude);
		estimateMapParams.put("passenger_phone", passenger_phone);
		estimateMapParams.put("in_coord_type", in_coord_type);	
		
		
		//ConcurrentHashMap<String,String> headerParams = new ConcurrentHashMap<String,String>();
		//String sign=getSign.getSignKey(estimateMapParams);
		//System.out.println("订单预估签名："+sign+"订单预估token"+accessToken);
		//System.out.println("订单预估请求参数："+getSign.doGetParamsStr(estimateMapParams));
		
	//	headerParams.put("sign", sign);	
	//	headerParams.put("Authorization", "Bearer "+accessToken);
	//	headerParams.put("User-Agent", "aWeidao/7.2.2 (X900; Android 5.0.2)");
		
		results.setSamplerData("Data:\n getEstimated:"+url+getSign.doGetParamsStr(estimateMapParams)+"Token:"+accessToken);
				
		try { 
			results.sampleStart();
			//String response=HttpUtils.getIntance().doSendGet(url,estimateMapParams,headerParams).toString();
			String response=UserAppApiInter.orderEstimate(estimateMapParams, accessToken);	
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
