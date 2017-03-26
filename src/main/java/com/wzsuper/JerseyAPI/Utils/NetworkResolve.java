package com.wzsuper.JerseyAPI.Utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 解析网卡Ip
 * 
 */
public class NetworkResolve {

	private static Logger logger = LoggerFactory.getLogger(NetworkResolve.class);

	// 缓存
	private static String serverIp;

	public static String getServerIp() {
		if (serverIp != null) {
			return serverIp;
		}
		// 一个主机有多个网络接口
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = netInterfaces.nextElement();
				// 每个网络接口,都会有多个"网络地址",比如一定会有loopback地址,会有siteLocal地址等.以及IPV4或者IPV6
				// .
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet6Address) {
						continue;
					}
					if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
						serverIp = address.getHostAddress();
						logger.info("resolve server ip:" + serverIp);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return serverIp;
	}

	public static String getLoaclAddress() {
		if (serverIp != null) {
			return serverIp;
		}
		try {
			serverIp = InetAddress.getLocalHost().getHostAddress();
			logger.info("resolve server ip:" + serverIp);
		} catch (Exception e) {
			logger.error("resolve server ip error:", e);
		}
		return serverIp;
	}

	public void reset() {
		serverIp = null;
	}


	public static void main(String args[]) {
		serverIp = NetworkResolve.getLoaclAddress();
		System.out.println("getLoaclAddress:" + serverIp);

		serverIp = NetworkResolve.getServerIp();
		System.out.println("getServerIp:" + serverIp);
	}

}
