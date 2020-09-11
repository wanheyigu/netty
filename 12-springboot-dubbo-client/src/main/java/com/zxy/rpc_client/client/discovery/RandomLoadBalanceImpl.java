package com.zxy.rpc_client.client.discovery;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
/*
 * 随机负载均衡策略
 * 
 * 注：servers需要通过服务发现获取
 */
@Component
public class RandomLoadBalanceImpl implements LoadBalance{

	@Override
	public String choose(List<String> servers) {

		String serverAddress = servers.get(new Random().nextInt(servers.size()));
		
		return serverAddress;
	}

}
