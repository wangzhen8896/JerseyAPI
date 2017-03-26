package com.wzsuper.JerseyAPI.Server.discovery;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.wzsuper.JerseyAPI.Beans.PR;
import com.wzsuper.JerseyAPI.Beans.ServiceInfo;
import com.wzsuper.JerseyAPI.Server.annotation.Register;
import com.wzsuper.JerseyAPI.Utils.ContainerUtil;
import com.wzsuper.JerseyAPI.Utils.NetworkResolve;
import com.wzsuper.JerseyAPI.Utils.NullOrEmptyUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import net.sf.json.JSONSerializer;

/**
 * 服务注册中心
 * @author wangzhen
 *
 */
@Service
@Path("services/registry")
public class ServiceRegistry extends ServiceDiscovery{

	//是否注册到zookeeper
	private static boolean zookeeper = false;

	//注入配置服务器ip
	private static String serverip = null;

	public void setZookeeper(boolean zookeeper) {
		ServiceRegistry.zookeeper = zookeeper;
	}

	public void setServerip(String serverip) {
		ServiceRegistry.serverip = serverip;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public PR getServer(@QueryParam("name") String name, @QueryParam("version") String version) {
		return super.getServer(name, version);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (zookeeper && (zkClient != null && zkClient.getState() == CuratorFrameworkState.LATENT)) {
			zkClient.start();
		}
		logger.info("开始注册服务");
		try {
			WebApplicationContext webcontext = (WebApplicationContext) event.getApplicationContext();
			servletContext =  webcontext.getServletContext();
			Map<String, Object> services = event.getApplicationContext().getBeansWithAnnotation(Service.class);
			for (String key : services.keySet()) {
				Class clazz = services.get(key).getClass();
				if (clazz.getName().indexOf("$") > 0) {
					clazz = clazz.getSuperclass();
				}
				register(clazz);
			}
			if(zookeeper){
				treeCache = new TreeCache(zkClient, ROOTPATH);
				treeCache.start();
				treeCache.getListenable().addListener(this);
			}
			
		} catch (Exception e) {
			logger.error("服务注册异常:", e);
		}
		logger.info("注册服务结束");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void register(Class clazz) {
		if (clazz.isAnnotationPresent(Path.class)) {
			Method[] methods = clazz.getMethods();
			String rootpath = "";
			Path p = (Path) clazz.getAnnotation(Path.class);
			rootpath = p.value();
			if (StringUtils.isNotBlank(rootpath) && rootpath.endsWith("/")) {
				rootpath = rootpath.substring(0, rootpath.lastIndexOf("/"));
			}
			for (Method method : methods) {
				ServiceInfo serviceinfo = new ServiceInfo();
				method.getParameterAnnotations();
				serviceinfo.setContextPath(servletContext.getContextPath()+"/api");
				String path = "";
				
				if (method.isAnnotationPresent(Register.class)) {
					Register register = method.getAnnotation(Register.class);
					String name = register.name();
					String version = register.version();
					if (StringUtils.isNotEmpty(name)) {
						serviceinfo.setName(name);
						serviceinfo.setVersion(version);
					} else
						continue;

					if (method.isAnnotationPresent(Path.class)) {
						path = method.getAnnotation(Path.class).value();
					}
					if (StringUtils.isNotBlank(path) && rootpath.startsWith("/")) {
						path = String.format("%s/%s", rootpath, path.replaceFirst("/", ""));
					} else
						path = rootpath;
					serviceinfo.setPath(path);
					if (method.isAnnotationPresent(GET.class)) {
						serviceinfo.setHttpMethod("GET");
					}
					if (method.isAnnotationPresent(POST.class)) {
						serviceinfo.setHttpMethod("POST");
					}
					if (method.isAnnotationPresent(PUT.class)) {
						serviceinfo.setHttpMethod("PUT");
					}
					if (method.isAnnotationPresent(DELETE.class)) {
						serviceinfo.setHttpMethod("DELETE");
					}
					if (method.isAnnotationPresent(HEAD.class)) {
						serviceinfo.setHttpMethod("HEAD");
					}
					String serverip = NetworkResolve.getLoaclAddress();
					Integer port = ContainerUtil.getServerPort();
					if(NullOrEmptyUtil.isNullOrEmpty(serverip)){
						throw new RuntimeException("获取服务器ip失败！");
					}
					String servernode = String.format("%s:%s", serverip, port);
					serviceinfo.setServer(servernode);
					if(!zookeeper){
						List<ServiceInfo> servicelist = new ArrayList<ServiceInfo>();
						servicelist.add(serviceinfo);
						services.put(ServiceInfo.getServicekey(name, version), servicelist);
					}else
						publish(serviceinfo);
					
					logger.info(String.format("注册服务%s成功,版本:%s,请求类型:%s,服务地址:%s", serviceinfo.getName(),
							serviceinfo.getVersion(), serviceinfo.getHttpMethod(), serviceinfo.getPath()));
				}
			}
		} else
			logger.warn("{} 未定义@Path,注册服务失败！", clazz.getName());
	}

	private boolean publish(ServiceInfo serviceinfo) {
		if(zkClient.getState() == CuratorFrameworkState.LATENT){  
			zkClient.start();  
		}  
		try {  
			String nodepath = String.format("/%s/%s/%s", serviceinfo.getName(), serviceinfo.getVersion(), serviceinfo.getServer());
			/*zkClient.create().creatingParentsIfNeeded()
			.withMode(CreateMode.EPHEMERAL) 
			.forPath(nodepath, JSONSerializer.toJSON(serverInfo).toString().getBytes());*/
			PersistentEphemeralNode node = new PersistentEphemeralNode(zkClient, PersistentEphemeralNode.Mode.EPHEMERAL, nodepath, JSONSerializer.toJSON(serviceinfo).toString().getBytes());
			node.start();
			node.waitForInitialCreate(3, TimeUnit.SECONDS);
		}  catch (Exception e) {  
			logger.error("register service address to zookeeper exception:{}",e); 
			return false;
		}  
		return true;
	}

	@Override
	public void close() throws IOException {
		zkClient.close();
		treeCache.close();
	}
}
