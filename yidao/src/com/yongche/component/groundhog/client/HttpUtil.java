package com.yongche.component.groundhog.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * http工具类
 * @author shaoxiangfei
 *
 */
public class HttpUtil {	

	protected CloseableHttpResponse response = null;
	
    private final CloseableHttpClient httpclient;
    private static final int SIZE = 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private static class HttpUtilHolder {
        private static final HttpUtil INSTANCE = new HttpUtil();
    }

    public static HttpUtil getIntance() {
        return HttpUtilHolder.INSTANCE;
    }

	private HttpUtil() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// 将最大连接数增加到300
		cm.setMaxTotal(300);
		// 将每个路由基础的连接增加到300
		cm.setDefaultMaxPerRoute(300);
		// 链接超时setConnectTimeout ，读取超时setSocketTimeout
		RequestConfig defaultRequestConfig = null;
		defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();		

		httpclient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(defaultRequestConfig)
				.build();

		ThreadUtil.submit(new IdleConnectionMonitorThread(cm));
	}

    /**
     * 编码默认UTF-8
     * @param url
     * @return
     */
    public String get(String url) {
        return this.get(url, CHARSET_UTF8.toString());
    }

    /**
     * @param url
     * @param code
     * @return
     */
    public String get(String url, final String code) {
        String res = null;

        try {
            HttpGet httpget = new HttpGet(url);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity, code) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            res = httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
            logger.error("url:{}",url, e);
        }
        return res;
    }

    public List<String> getList(String url) {
        List<String> res = null;
        try {
            HttpGet httpget = new HttpGet(url);
            ResponseHandler<List<String>> responseHandler = new ResponseHandler<List<String>>() {
                @Override
                public List<String> handleResponse(final HttpResponse response) throws ClientProtocolException,
                        IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        List<String> result = new ArrayList<String>();
                        HttpEntity entity = response.getEntity();
                        if (entity == null) {
                            return result;
                        }
                        BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()), SIZE);
                        while (true) {
                            String line = in.readLine();
                            if (line == null) {
                                break;
                            } else {
                                result.add(line);
                            }
                        }
                        in.close();
                        return result;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            res = httpclient.execute(httpget, responseHandler);
        } catch (Exception e) {
            logger.error(url, e);
        }
        return res;
    }

    private String post(String url, List<NameValuePair> params, String code) {
        String res = null;
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(url);
            if (params != null)
                httpPost.setEntity(new UrlEncodedFormEntity(params, code));
            response = httpclient.execute(httpPost);
            HttpEntity entity2 = response.getEntity();
            res = EntityUtils.toString(entity2, code);
            EntityUtils.consume(entity2);
        } catch (Exception e) {
            logger.error(url, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
        }
        return res;
    }

    private static final String APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_TEXT_JSON = "text/json";

    public String postJSON(String url, String json, String code) {
      String res = null;
      CloseableHttpResponse response = null;
      try {
          HttpPost httpPost = new HttpPost(url);
          httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
          
          StringEntity se = new StringEntity(json);
          se.setContentType(CONTENT_TYPE_TEXT_JSON);
          se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
          httpPost.setEntity(se);
          response = httpclient.execute(httpPost);
          HttpEntity entity2 = response.getEntity();
          res = EntityUtils.toString(entity2, code);
          EntityUtils.consume(entity2);
      } catch (Exception e) {
          logger.error(url, e);
      } finally {
          if (response != null) {
              try {
                  response.close();
              } catch (IOException e) {
              }
          }
      }
      return res;
  }

    
    
    /**
     * 默认UTF-8
     * @param url
     * @param params
     * @return
     */
    public String post(String url, Map<String, ?> params) {
        return this.post(url, params, CHARSET_UTF8.toString());
    }

    /**
     * @param url{
     * @param params
     * @param code
     * @return
     */
    public String post(String url, Map<String, ?> params, String code) {
        List<NameValuePair> nvps = null;
        if (params != null && params.size() > 0) {
            nvps = new ArrayList<NameValuePair>();
            for (Entry<String, ?> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
        }
        return this.post(url, nvps, code);
    }

    // 监控有异常的链接
    private static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;
        private volatile boolean shutdown;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        // 关闭失效的连接
                        connMgr.closeExpiredConnections();
                        // 可选的, 关闭30秒内不活动的连接
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public String postBody(String url, String body) {
        String res = null;
        try {
            HttpPost httppost = new HttpPost(url);
            if (StringUtils.isNotBlank(body)) {
                httppost.setEntity(new StringEntity(body, CHARSET_UTF8));
            }
            CloseableHttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                res = EntityUtils.toString(resEntity, CHARSET_UTF8);
                EntityUtils.consume(resEntity);
            }
        } catch (Exception e) {
            logger.error(url, e);
        }

        return res;
    }
    
    public Object doSendPostOAuth(String url, Map<String,String>mapParams,Map<String,String> headerParams,String userAgent) {
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		if(mapParams!=null){
			for(Map.Entry<String, String> entry:mapParams.entrySet()){
				 BasicNameValuePair kvPair = new BasicNameValuePair(entry.getKey(), entry.getValue());
				 list.add(kvPair);
				}
			System.out.println("抢单司机请求："+userAgent+"\n抢单司机postParams参数:"+getPostParameter(mapParams));
		}
	
		HttpPost httpPost = new HttpPost(strParseToUri(url));
		StringBuffer sb = new StringBuffer();
		if(headerParams!=null){			
			for(Map.Entry<String, String> entry:headerParams.entrySet()){
				sb.append(entry.getKey()+"="+entry.getValue()+",");				
			}
		}
		String header = sb.substring(0, sb.length()-1);
		//System.out.println("OAuth header惨数打印："+header);
		httpPost.addHeader("Authorization","OAuth "+header);
		httpPost.addHeader("Content-Type","application/x-www-form-urlencoded");
		httpPost.setHeader("Accept-Encoding", "gzip");
		httpPost.setHeader("User-Agent",userAgent);
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
			
			return doExecute(httpPost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public Object doSendPostOAuthcf(String url, Map<String,String>mapParams,Map<String,String> headerParams) {
    	List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		if(mapParams!=null){
			for(Map.Entry<String, String> entry:mapParams.entrySet()){
				 BasicNameValuePair kvPair = new BasicNameValuePair(entry.getKey(), entry.getValue());
				 list.add(kvPair);
				}
		//	System.out.println("postParams:"+getPostParameter(mapParams));
		}
		HttpPost httpPost = new HttpPost(strParseToUri(url));
		
		for(Entry<String,String> e:headerParams.entrySet()){
			httpPost.addHeader(e.getKey(),e.getValue());
		}
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
			return doExecute(httpPost);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
	public static String getPostParameter(Map<String,String>map){
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, String> entry:map.entrySet()){
			sb.append(entry.getKey()+"="+entry.getValue()+"&");
		}
		return sb.substring(0,sb.length()-1);
	}
	
	public URI strParseToUri(String url){
		try{
			URL toURL = new URL(url); 
			URI uri = new URI(toURL.getProtocol(), toURL.getHost(), toURL.getPath(), toURL.getQuery(), null); 
			return uri;	
		}catch(Exception e){
			
		}
		return null;
	}
	
	private Object doExecute(HttpUriRequest request) {
		String rs = null;

//		response = null;
		try {
		//	System.out.println(request.getURI());

			Header[] headers = request.getAllHeaders();
		//	System.out.println("抢单司机header信息");
//		    for(Header header:headers){
//				System.out.println(header.getName()+"  "+header.getValue());
//			}

			response =httpclient.execute(request);

			HttpEntity entityStr = response.getEntity();
			rs = EntityUtils.toString(entityStr,"UTF-8").trim();
			EntityUtils.consume(entityStr);

		} catch (Exception e) {
           rs = e.getMessage();
		}		
		return rs;
	}
}