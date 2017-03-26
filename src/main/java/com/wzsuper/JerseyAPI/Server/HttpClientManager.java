package com.wzsuper.JerseyAPI.Server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * HttpClient
 * 连接池需优化，可配置
 * @author wangzhen
 */  
public class HttpClientManager{

	final static Logger logger = LoggerFactory.getLogger(HttpClientManager.class);

	private PoolingHttpClientConnectionManager cm;
  
    /** 
     * 最大连接数 
     */  
    public final static int MAX_TOTAL_CONNECTIONS = 800;  
    /** 
     * 获取连接的最大等待时间 
     */  
    public final static int WAIT_TIMEOUT = 60000;  
    /** 
     * 每个路由最大连接数 
     */  
    public final static int MAX_ROUTE_CONNECTIONS = 400;  
    /** 
     * 连接超时时间 
     */  
    public final static int CONNECT_TIMEOUT = 10000;  
    /** 
     * 读取超时时间 
     */  
    public final static int READ_TIMEOUT = 10000;  
  
    
    @PostConstruct
    public void init() {
        LayeredConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        cm =new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
    }

    public CloseableHttpClient getHttpClient() {       
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();          
        return httpClient;
    }
    
    public static JSONObject getContent(HttpResponse response){
    	JSONObject json = new JSONObject();
    	if(response != null){
			ByteArrayOutputStream baos = null;
			InputStream is = null;
			try {
				HttpEntity entity = response.getEntity();
				if(entity != null){
					baos = new ByteArrayOutputStream();
					is = entity.getContent();
					byte[] buf = new byte[1024];
					int len = -1;
					while ((len = is.read(buf)) != -1) {  
						baos.write(buf, 0, len); 
					}  
				}
				json.put("status", response.getStatusLine().getStatusCode());
				json.put("content", new String(baos.toByteArray(),"UTF-8"));
			}catch (Exception e) {
				logger.error("" + e);
				json.put("error", e.getMessage());
			}finally {
					try {
						if(baos != null) baos.close();
						if(is != null) is.close();
					} catch (IOException e) {
						logger.error("" + e);
						json.put("error", e.getMessage());
					}
			}
		}
    	return json;
    }
    
  
}  