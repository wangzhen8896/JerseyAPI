package com.wzsuper.JerseyAPI.Beans;

import java.io.Serializable;

public class ServiceInfo implements Serializable {

	private static final long serialVersionUID = -317650657045001859L;

	private static transient final  String formatServicekey = "%s_%s";
	
	public static transient final  String defaultVersion = "1.0.0";
	
	private String name;
	
	private String version;
	
	private String httpMethod;
	
	private String path;
	
	private String contextPath;
	
	private boolean stateless;
	
	private String server;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public boolean isStateless() {
		return stateless;
	}

	public void setStateless(boolean stateless) {
		this.stateless = stateless;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public static String getServicekey(String name, String version){
		if(version == null)
			version = defaultVersion;
		return String.format(formatServicekey, name, version);
	}
	
}
