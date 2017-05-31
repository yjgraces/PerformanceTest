package letv.jmeter.yidao;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetYibotSign {
	static ArrayList<Question> questList = new ArrayList<Question>();
	static Question questionInfo;
	
	public static String getSign(HashMap<String,String> map){
		String md5str = null;
		try {
			String str=GetYibotSign.sort(map);
			String bs64Str = GetYibotSign.encodeBase64(str);
			md5str=GetYibotSign.encodeMD5(bs64Str);
			
			System.out.println("request parameter:"+GetYibotSign.doGetParamsStr(map));
			System.out.println(" raw_str: "+str+"\n base64: "+bs64Str+"\n md5: "+md5str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return md5str;		
	}
	
	public static String getSign_new(String quest){
		String md5str = null;
		HashMap<String,String> map=new HashMap<String,String>();
		
		map.put("account", "acct123");
		map.put("cid", "123");
		map.put("ip", "120.24.19.73");
		map.put("pubkey", "VNNJ52PDeoVA3PvIYjvvMoq5FFWZBSgH2RADfS9UjW0");
		map.put("question",quest);
		//map.put("question","我要打车");
		map.put("sessionId", "sid123");
		map.put("source", "test");
		
		try {
			String str=GetYibotSign.sort(map);
			String bs64Str = GetYibotSign.encodeBase64(str);
			String urlcode=GetYibotSign.URLEncoder(quest);
			
			md5str="question:\""+quest+"\",md5:\""+GetYibotSign.encodeMD5(bs64Str)+"\""+",\"urlEncode:"+urlcode+"\"";
			
		//	System.out.println("request parameter:"+GetYibotSign.doGetParamsStr(map));
		//	System.out.println(" raw_str: "+str+"\n base64: "+bs64Str+"\n md5: "+md5str);

			System.out.println(md5str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return md5str;		
	}
	
	
	@SuppressWarnings("unchecked")
	/*
	 * 将所有参数按照key进行字典排序，然后将其value按照排好的顺序拼接起来，
	 * 最后再拼接上用户的私钥，拼接后的结果便是计算签名的原始串raw_str
	 */
	public static String sort(HashMap<String,String> map){
		String raw_str="";
		StringBuffer sb = new StringBuffer();
		String priv_key="8b5828a5cddc043694f7ca8971892be3";

		Collection<String> keyset= map.keySet(); 
		List list=new ArrayList<String>(keyset);
		Collections.sort(list);
		
		for(int i=0;i<list.size();i++){
			sb.append(map.get(list.get(i)));		
		}
		raw_str=sb.toString()+priv_key;
		//System.out.println("raw_str:=========="+raw_str);
		return raw_str;

	}
			
	
	/*** 
     * encode by Base64 
     */  
    public static String encodeBase64(String inputString) throws Exception{ 
    	
    	byte[] encodeBase64 = Base64.getEncoder().encode(inputString.getBytes("UTF-8"));  
        return new String(encodeBase64);  
    }  
    /*** 
     * decode by Base64 
     */  
    public static String decodeBase64(String input) throws Exception{  
        Class clazz=Class.forName("com.sun.org.apache.xerces.internal.impl.dv.util.Base64");  
        Method mainMethod= clazz.getMethod("decode", String.class);  
        mainMethod.setAccessible(true);  
        Object retObj=mainMethod.invoke(null, input);  
        String decodeStr=new String((byte[])retObj);
 
        return decodeStr;
    } 
    
	
	public static String encodeMD5(String inStr){ 
		MessageDigest md5 = null; 
		try{ 
			md5 = MessageDigest.getInstance("MD5"); 
		}catch (Exception e){ 
			System.out.println(e.toString()); 
			e.printStackTrace(); 
		return ""; 
		} 
		char[] charArray = inStr.toCharArray(); 
		byte[] byteArray = new byte[charArray.length]; 

		for (int i = 0; i < charArray.length; i++){
			byteArray[i] = (byte) charArray[i]; 
		}
		
		byte[] md5Bytes = md5.digest(byteArray); 
		StringBuffer hexValue = new StringBuffer(); 
		
		for (int i = 0; i < md5Bytes.length; i++){ 
			int val = ((int) md5Bytes[i]) & 0xff; 
			if (val < 16) 
			hexValue.append("0"); 
			hexValue.append(Integer.toHexString(val)); 
		} 
		return hexValue.toString(); 

	} 
	
	   public static String doGetParamsStr(Map<String, String> params) {
			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (!"".equals(entry.getValue()) && !"null".equals(entry.getValue())) {
					sb.append(entry.getKey() + "=" + entry.getValue() + "&");
				}
			}
			return sb.substring(0, sb.length()-1);
		}
	   
	   
	  //从文件读取question信息	
		public static void  getQuestionInfo(){

			String filepath=System.getProperty("user.dir")+"/quest.dat";

		//	System.out.println(System.getProperty("user.dir"));
			File file = new File(filepath);

			BufferedReader breader =null;
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(new FileInputStream(file), "GBK");
				breader = new BufferedReader(reader);
				
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String line = "";
			try {
				while ((line = breader.readLine()) != null) {
					questionInfo = new Question();				
				String[] values = line.split(",");
				questionInfo.setQuest_info((values[0]));				
				questList.add(questionInfo);
							
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//URL encode
		public String URLDecoder(String str){
	        // 将application/x-www-from-urlencoded字符串转换成普通字符串  
			
	        String keyWord = null;
			try {
				keyWord = URLDecoder.decode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
	        System.out.println(keyWord);
			return keyWord;  
	    
		}
		
		// 将普通字符创转换成application/x-www-from-urlencoded字符串  
		public static String URLEncoder(String str){
			String urlString = null;
			try {
				urlString = URLEncoder.encode(str, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return urlString;			
		}
		
		public static void main(String[] args) {
			
			GetYibotSign yb=new GetYibotSign();
			
//			String quest="我要打车";
//			String quest2="无聊";
			
			GetYibotSign.getQuestionInfo();
			
			//System.out.println("List长度："+questList.size());
			System.out.println("question,sign");
			
			for(int i=0;i<questList.size();i++){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String quest=questList.get(i).getQuest_info();	
				//System.out.println(quest);
				String md5=GetYibotSign.getSign_new(quest);
				String urlcode=GetYibotSign.URLEncoder(quest);
				
				System.out.println(quest+","+urlcode+","+md5);

		}
	}

}
