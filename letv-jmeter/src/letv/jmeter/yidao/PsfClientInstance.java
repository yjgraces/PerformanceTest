package letv.jmeter.yidao;

import com.yongche.psf.client.ClientManager;

/**
 * Created by kuicui on 2017/4/13.
 */
public class PsfClientInstance {


    public String getInstance(String url,String serviceType,String version,String[] serviceCenter,int size){
        ClientManager client = new ClientManager(serviceType,version,serviceCenter);
        client.setPkgSize(64*1024);
        client.setPoolSize(size);
        client.init();
        String rs = null;
		try {
			rs = client.call(url,null,null);
			client.destroy();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return rs;
    }

    public static void main(String[] args) throws Exception {
    	PsfClientInstance p=new PsfClientInstance();
        String[] hosts = {"10.0.11.71:5201","10.0.11.72:5201"};
        String url="/activity/findActivityRewards?cityCode=sz&driverId=50062620";
        String rs1=p.getInstance(url,"activity", "1.0", hosts,3);
        System.out.println(rs1);

    }
}
