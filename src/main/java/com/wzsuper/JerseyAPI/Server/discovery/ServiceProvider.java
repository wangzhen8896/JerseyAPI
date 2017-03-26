package com.wzsuper.JerseyAPI.Server.discovery;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 服务提供
 * zkClient需指定namespace
 * @author wangzhen
 */
public class ServiceProvider extends ServiceDiscovery{

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			if (zkClient.getState() == CuratorFrameworkState.LATENT) {
				zkClient.start();
			}
			treeCache = new TreeCache(zkClient, ROOTPATH);
			treeCache.start();
			treeCache.getListenable().addListener(this);
		} catch (Exception e) {
			logger.error("服务异常: ", e);
		}
	}

	@Override
	public void close() {
		try {
			treeCache.close();
			zkClient.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}