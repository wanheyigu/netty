package com.zxy.rpc_client.client.discovery;

import java.util.List;

public interface LoadBalance {

	/*
	 * 从服务提供者列表servers中，通过负载均衡策略获取一个服务提供者的主机地址
	 */
	String choose(List<String> servers);
}
