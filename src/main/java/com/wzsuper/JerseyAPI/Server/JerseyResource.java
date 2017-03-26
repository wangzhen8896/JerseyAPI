package com.wzsuper.JerseyAPI.Server;

import org.glassfish.jersey.server.ResourceConfig;

import com.wzsuper.JerseyAPI.Server.discovery.ServiceRegistry;
import com.wzsuper.JerseyAPI.Server.provider.JSONMessageBodyProvider;

public class JerseyResource extends ResourceConfig {
	
	public JerseyResource(){
		 packages("com.longrise.JerseyAPI.Server");
	     register(JSONMessageBodyProvider.class);
	     //register(JacksonFeature.class);
	     register(ServiceRegistry.class);
	}

}
