package letv.jmeter.yidao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Yibot_test {


	public static void main(String[] args) {

		Yibot_test y=new Yibot_test();
		String quest="我要打车"; 
		y.yibot_request(quest);
		String sign=GetYibotSign.getSign_new(quest);
			
	}
	
	
	public String yibot_request(String quest){
		
		HashMap<String,String> map=new HashMap<String,String>();
		
		String url="http://106.75.95.113/yibot/query";

			map.put("account", "acct123");
			map.put("cid", "123");
			map.put("ip", "120.24.19.73");
			map.put("pubkey", "VNNJ52PDeoVA3PvIYjvvMoq5FFWZBSgH2RADfS9UjW0");
			map.put("question",quest);
			//map.put("question","我要打车");
			map.put("sessionId", "sid123");
			map.put("source", "test");
				
			//ip=120.24.19.73&account=acct123&sessionId=sid123&source=test
			
			String sign=GetYibotSign.getSign(map);
			//String sign=GetYibotSign.getSign_new(quest);
			System.out.println("sign:======="+sign);
			
		//	String sign="f075f131151f82e675ff69e5a397048b";
			map.put("sign", sign);
           System.out.println(map.toString());
				
			Object rs = HttpUtils.getIntance().doSendGet(url,map);	
			String response=rs.toString();
			System.out.println("response:===="+rs.toString());
			return response;
	}
}
