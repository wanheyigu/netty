package com.zxy.rpc_client.client.discovery;


/** 
 * 服务发现
 */
public interface ServiceDiscovery {

	/** 服务发现：
	 * 根据服务名称查找提供者主机地址
	 * @param serviceName
	 * @return  ip:port
	 * @throws Exception 
	 */
	String discovery(String serviceName) throws Exception;
}
