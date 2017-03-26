package com.wzsuper.JerseyAPI.Server.discovery;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import com.wzsuper.JerseyAPI.Beans.PR;
import com.wzsuper.JerseyAPI.Beans.ServiceInfo;
import com.wzsuper.JerseyAPI.Utils.NullOrEmptyUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import net.sf.json.JSONObject;

/**
 * 服务发现
 * @author wangzhen
 *
 */
public abstract class ServiceDiscovery implements ApplicationListener<ContextRefreshedEvent>,TreeCacheListener, Closeable {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final static String ROOTPATH = "/";
	
	protected Map<String, List<ServiceInfo>> services = new HashMap<String, List<ServiceInfo>>();
	
	protected TreeCache treeCache;
	
	protected CuratorFramework zkClient;
	
	protected ServletContext servletContext;
	
	public void setZkClient(CuratorFramework zkClient) {
		this.zkClient = zkClient;
	}
	
	
	@Override
	public abstract void onApplicationEvent(ContextRefreshedEvent event);
	
	/**
	 * 获取服务server
	 * @param servicename
	 * @param version
	 * @return
	 */
	public PR getServer(String name, String version){
		String server = "服务未找到";
		PR pr = new PR(0, server, null);
		List<ServiceInfo> serverlist = services.get(ServiceInfo.getServicekey(name, version));
		if(serverlist != null && serverlist.size() > 0){
			ServiceInfo serviceinfo = serverlist.get(RandomUtils.nextInt(serverlist.size()));
			pr.setResultstate(1);
			pr.setResultdesc("获取服务成功");
			pr.setResult(serviceinfo);
			server = serviceinfo.getServer();
		}
		logger.info("获取服务:{}, 版本:{}, 服务节点: {}", name, version, server);
		return pr;
	}
	
	
	protected TreeCache getTreeCache(){
		return treeCache;
	}
	
	protected void initCache(){
		Map<String, ChildData> servicesNode = treeCache.getCurrentChildren(ROOTPATH);
		for (String servicename : servicesNode.keySet()) {
			loadService(ROOTPATH + servicename);
		}
	}
	
	/**
	 * 加载service节点
	 * @param nodepath
	 */
	protected void loadService(String nodepath){
		Map<String, ChildData> versions = getTreeCache().getCurrentChildren(nodepath);
		for (String version : versions.keySet()) {
			loadServiceVersions(ZKPaths.getNodeFromPath(nodepath), nodepath + "/" + version);
		}
	}
	
	/**
	 * 加载服务版本
	 * @param name 服务名称
	 * @param nodepath 服务版本zkpath
	 */
	protected void loadServiceVersions(String name, String nodepath){
		Map<String, ChildData> servernode = treeCache.getCurrentChildren(nodepath);
		loadServerList(ServiceInfo.getServicekey(name, ZKPaths.getNodeFromPath(nodepath)), servernode);
	}
	
	/**
	 * 加载服务列表
	 * @param servicekey 
	 * @param servernode
	 */
	protected void loadServerList(String servicekey, Map<String, ChildData> servernode){
		List<ServiceInfo> list = new ArrayList<ServiceInfo>();
		for (String serverAddress : servernode.keySet()) {
			String[] address = serverAddress.split(":");
			ServiceInfo serverinfo = null;
			if(address != null && address.length == 2){
				serverinfo = (ServiceInfo) JSONObject.toBean(JSONObject.fromObject(new String(servernode.get(serverAddress).getData())), ServiceInfo.class);
				serverinfo.setServer(serverAddress);
				list.add(serverinfo);
			}
		}
		services.put(servicekey, list);
	}
	
	protected void reloadServerList(TreeCacheEvent event){
		String path = event.getData().getPath();
		if(NullOrEmptyUtil.isNotEmpty(path)){
			String[] nodes = path.split("/");
			switch (nodes.length - 1) {
			case 1:
				loadService(path);
				break;
			case 2:
				loadServiceVersions(nodes[1], path);
				break;
			case 3:
				loadServiceVersions(nodes[1], path.substring(0, path.lastIndexOf("/")));
				break;
			}
		}
	}
	
	@Override
	public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
		 switch (event.getType()) {
         case NODE_ADDED: {
        	 if(this instanceof ServiceProvider && event.getType() == TreeCacheEvent.Type.NODE_ADDED){
				logger.info("{} 发现服务: {} , data: {}", this.zkClient.getNamespace(), event.getData().getPath(), new String(event.getData().getData()));
			 }
         	 reloadServerList(event);
             break;
         }
         case NODE_UPDATED: {
        	 logger.info("{} 更新服务: {} , data: {}", client.getNamespace(), event.getData().getPath(), new String(event.getData().getData()));
        	 reloadServerList(event);
             break;
         }
         case NODE_REMOVED: {
        	 logger.info("{} 移除服务: {}", client.getNamespace(), event.getData().getPath());
         	 reloadServerList(event);
             break;
         }
         default:
        	 logger.info("{} 服务事件: {}", client.getNamespace(), event.getType());
        	 logger.info("{} 服务列表: {}", client.getNamespace(), JSONObject.fromObject(services).toString());
         }
	}
}
