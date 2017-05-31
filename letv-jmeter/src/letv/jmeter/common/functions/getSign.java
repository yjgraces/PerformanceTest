package letv.jmeter.common.functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class getSign {
	   /**
	    * 1.得到Nonce
	    * 2.key=value 按ascll码排序 
	    * 3.加nonce 加key=1bW4KASQPawc2G8DNBtIJmr35SzRcBCb
	    * 4.md5上页面串
	    * @throws NoSuchAlgorithmException 
	    */

		String key="1bW4KASQPawc2G8DNBtIJmr35SzRcBCb";	
		
		//获取 signkey	
		   
		   public  String getSignKey(Map<String,String> mapParams){
			   getSign getSign=new getSign();
			   String paraString=doGetParamsStr(mapParams);
			   paraString=getSign.encyptString(paraString)+"&key=1bW4KASQPawc2G8DNBtIJmr35SzRcBCb";			   
			  String signkey= getSign.md5Encypt(paraString);
			return signkey;
			   			   
		   }
		   
		   public String getSignKeyAsString(String paraString){
			  // String paraString=doGetParamsStr(mapParams);
			   getSign getSign=new getSign();
			  String paraString1=getSign.encyptString(paraString)+"&key=1bW4KASQPawc2G8DNBtIJmr35SzRcBCb";			   
			  String signkey= getSign.md5Encypt(paraString1);
			return signkey;
			   			   
		   }
		
		
		public String md5Encypt(String encyptString){  //md5加密
			String md5String="";
			try {
				 MessageDigest  md = MessageDigest.getInstance("MD5");
				 md.update(encyptString.getBytes("utf-8"));
				  byte b[] = md.digest();
				  StringBuffer buf = new StringBuffer("");
				 int i=0; 
				  for (int offset = 0; offset < b.length; offset++) {
					  i = b[offset];
					  if (i < 0) 
						  i += 256;
					  if (i < 16)
						  buf.append("0");
					  buf.append(Integer.toHexString(i)); 
					  }
				  md5String=buf.toString().toUpperCase();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return  md5String;
		}
		 
		   
		 public String encyptString(String paraString){    //param排序 加Nonce
			 String encyptStr="";
			 if(paraString.toLowerCase().contains("nonce")){
				 encyptStr=paraString;
			 }else{
				 encyptStr=paraString+"&nonce="+((System.currentTimeMillis()/1000)+"");
			 }			   
			 encyptStr=getSortParams(encyptStr);		
			 return encyptStr;
		 }
		 
	 
		 public  String getSortParams(String params) {  //param排序 
		        String result = "";
		        ArrayList<String> paramList = new ArrayList<String>();	       
		            String[] paramArray = params.split("&");
		            if (paramArray.length > 0) {
		                for (int i = 0; i < paramArray.length; i++) {
		                    String param = paramArray[i];
		                    if (param.contains("=")) {
		                        String value = getElementFromArray(param.split("="), 1);	                       
		                            paramList.add(param);	                     
		                }
		                Collections.sort(paramList);
		            }

		            StringBuilder builder = new StringBuilder();
		            for (int i = 0; i < paramList.size(); i++) {
		                builder.append(paramList.get(i) + "&");
		            }
		            result = builder.toString();
		            if (paramList.size() > 0){
		                result = result.substring(0, result.length() - 1);   
		                }         
		            }
		        return result;
		    }
		 
		   public static  <T> T getElementFromArray(T[] array, int index) {
		        if (isArrayEmpty(array)) {
		            return null;
		        }

		        if (index < 0 || index >= array.length) {
		            return null;
		        }

		        return array[index];
		    }
		   public static <T> boolean isArrayEmpty(T[] array) {
		        return array == null || array.length == 0;
		    }
		

	
	// getPostParameter	   
			public  String getPostParameter(Map<String, String> map) {
				StringBuffer sb = new StringBuffer();
				Iterator arg2 = map.entrySet().iterator();

				while (arg2.hasNext()) {
					Entry entry = (Entry) arg2.next();
					sb.append((String) entry.getKey() + "=" + (String) entry.getValue() + "&");
				}

				return sb.substring(0, sb.length() - 1);
			}
			
	// getSignPostParameter			
		   public  String getSignPostParameter(Map<String,String> mapParams){
			   getSign getSign=new getSign();
			   String p=getPostParameter(mapParams);
			   String signParameter=getSign.encyptString(p);
			   
			return signParameter;
			   
		   }
		   
	// getParameterStr		   
			public  String doGetParamsStr(Map<String, String> params) {
				StringBuffer sb = new StringBuffer();
				Iterator arg3 = params.entrySet().iterator();

				while (arg3.hasNext()) {
					Entry entry = (Entry) arg3.next();
					if (!"".equals(entry.getValue()) && !"null".equals(entry.getValue())) {
						sb.append((String) entry.getKey() + "=" + (String) entry.getValue() + "&");
					}
				}

				return sb.substring(0, sb.length() - 1);
			}
	//readfile as map from file
			public  Map<String,String> getParameterFromFileAsMap(String fileName){
				String filePath = System.getProperty("user.dir")+"\\"+fileName;
				//	String filePath = System.getProperty("user.dir")+"/"+fileName;
				System.out.println(filePath);
				File file = new File(filePath);
				Map<String,String> paramsMap = new HashMap<String,String>();
				BufferedReader breader =null;
				InputStreamReader reader = null;
				try{
					reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
					breader = new BufferedReader(reader);		
					String line = "";
					while((line=breader.readLine())!=null){
					String [] params = line.split("#");
					if(params.length==2){			
						paramsMap.put(params[0].trim(), params[1].trim());
					}else{
						paramsMap.put(params[0].trim(), "");	
					}
				}
					breader.close();
					reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				return paramsMap;	
			}
}