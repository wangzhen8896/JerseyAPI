package com.wzsuper.JerseyAPI.Server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wzsuper.JerseyAPI.Beans.PR;
import com.wzsuper.JerseyAPI.Beans.ServiceInfo;
import com.wzsuper.JerseyAPI.Server.discovery.ServiceProvider;

import net.sf.json.JSONObject;

/**
 * 远程服务代理
 * @author wangzhen
 */
public class RemoteServerProxy {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoteServerProxy.class);
	
	String scheme = "http";
	
	ServiceProvider serviceProvider;
	
	HttpClientManager httpClientManager;
	
	public void setScheme(String scheme){
		this.scheme = scheme;
	}
	
	public void setServiceProvider(ServiceProvider serviceProvider){
		this.serviceProvider = serviceProvider;
	}
	
	public void setHttpClientManager(HttpClientManager httpClientManager){
		this.httpClientManager = httpClientManager;
	}
	
	public JSONObject callservice(String name, String version, JSONObject data) throws URISyntaxException{
		JSONObject result = new JSONObject();
		PR server = serviceProvider.getServer(name, version);
		if (server != null && server.getResultstate() == 1) {
			ServiceInfo serviceinfo = (ServiceInfo) server.getResult();
			if(serviceinfo != null){
				HttpRequestBase request = null;
				CloseableHttpClient httpClient = httpClientManager.getHttpClient();
				URI uri = BuildURI(serviceinfo, data);
				String httpMethod = serviceinfo.getHttpMethod();
				switch (httpMethod) {
				case HttpGet.METHOD_NAME:
					request = new HttpGet(uri);
					break;
				case HttpPost.METHOD_NAME:
					HttpPost post = new HttpPost(uri);
					post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
					post.setEntity(new StringEntity(data.toString(), "UTF-8"));
					request = post;
					break;
				case HttpPut.METHOD_NAME:
					HttpPut put = new HttpPut(uri);
					put.setHeader("Content-Type", MediaType.APPLICATION_JSON);
					put.setEntity(new StringEntity(data.toString(), "UTF-8"));
					request = put;
					break;
				case HttpDelete.METHOD_NAME:
					request = new HttpDelete(uri);
					break;
				case HttpHead.METHOD_NAME:
					request = new HttpHead(uri);
					break;
				}
				CloseableHttpResponse response = null;
				try {
					response = httpClient.execute(request);
					result = HttpClientManager.getContent(response);
				} catch (ClientProtocolException e) {
					logger.error("调用远程服务异常：" + e);
				} catch (IOException e) {
					logger.error("调用远程服务异常：" + e);
				}finally {
					try {
						if(response != null) response.close();
					} catch (IOException e) {
						logger.error("HttpClient关闭异常:", e);
					}
				}
			}
		}
		return result;
	}
	
	public URI BuildURI(ServiceInfo serviceinfo, JSONObject data) throws URISyntaxException{
		URIBuilder uri = new URIBuilder().setScheme(scheme)
						.setHost(serviceinfo.getServer());
		String path = serviceinfo.getPath();
		StringBuffer serverpath = new StringBuffer(serviceinfo.getContextPath());
		if(path != null){
			String[] pathparam = path.split("/\\{");
			for (int i = 0; i < pathparam.length; i++) {
				if(pathparam[i].endsWith("}")){
					String key = pathparam[i].replace("}", "");
					pathparam[i] = "/" + data.getString(key);
					data.remove(key);
				}
				serverpath.append(pathparam[i]);
			}
			uri.setPath(serverpath.toString());
			@SuppressWarnings("unchecked")
			Iterator<String> it = data.keys();
			while (it.hasNext()) {
				String key = it.next();
				String value = data.getString(key);
				uri.addParameter(key, value);
			}
		}
		return uri.build();
	}
}
