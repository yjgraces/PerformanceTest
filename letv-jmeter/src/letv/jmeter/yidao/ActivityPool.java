//package letv.jmeter.yidao;
//
//import org.apache.jmeter.config.Arguments;
//import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
//import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
//import org.apache.jmeter.samplers.SampleResult;
//
//import com.yongche.psf.client.ClientManager;
//
//import java.io.Serializable;
//
//public class ActivityPool extends AbstractJavaSamplerClient implements Serializable {
//	String[] serviceCenter;
//    String serviceType;
//    String version;
//    String service_uri;
//    String name;
//    ClientManager client;
//
//
//	@Override
//	public void setupTest(JavaSamplerContext context) {
//        serviceCenter = context.getParameter("serviceCenter").split(",");
//        service_uri = context.getParameter("service_uri");
//        serviceType = context.getParameter("serviceType");
//        version = context.getParameter("version");
//     //   client = PsfClientInstance.getInstance(serviceType, version, serviceCenter);
//
//	}
//
//	@Override
//	public Arguments getDefaultParameters() {
//		Arguments params = new Arguments();
//		params.addArgument("serviceCenter", "10.0.11.71:5201,10.0.11.72:5201");
//		params.addArgument("service_uri", "/activity/findActivityInf?cityCode=sz&driverId=50062620");
//		params.addArgument("serviceType", "activity");
//		params.addArgument("version", "1.0.0");
//		return params;
//	}
//
//
//    @Override
//    public SampleResult runTest(JavaSamplerContext context) {
//        SampleResult results = new SampleResult();
//        results.setSampleLabel(name);
//        String service_uri = "/activity/findActivityRewards?cityCode=sz&driverId=50062620";
//        results.setSamplerData("Data:\n" + service_uri);
//        try {
//            results.sampleStart();
//            String response = client.call(service_uri, null,null);
//            results.setResponseData(response, "UTF-8");
//            //System.out.println( "creatorder"+ request.service_uri);
//            System.out.println( "response:"+ response);
//            results.setResponseCodeOK();
//            results.setSuccessful(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//            results.setSuccessful(true);
//            results.setResponseData(e.toString(), "UTF-8");
//        } finally {
//            results.sampleEnd();
//        }
//        return results;
//    }
//
//
//
//
//
//
//
//}
