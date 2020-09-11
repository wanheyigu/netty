package com.zxy.rpc.consumer;

import com.zxy.rpc.client.RpcProxy;
import com.zxy.service.SomeService;

/**
 * 客户端消费者
 *
 */
public class SomeConsumer {

	public static void main(String[] args) {
		/** 客户端消费者调用流程：
		 * 1.通过代理对象创建业务调用接口对象；
		 * SomeService service = 代理对象；
		 * 2.进行远程调用
		 * service.method(params);
		 * 
		 * "需要将业务接口通知到Server与Client"
		 * SomeService接口服务端与客户端都需要，因此定义到rpc-api工程中
		 */
		/* 1.通过client端代理对象创建接口
		 * "Client端只需根据业务接口名就可获取到Server端发布的服务提供者"
		 */
		SomeService service = RpcProxy.create(SomeService.class);
		String result = service.hello("Tom");
		
		System.out.println(result);
		
		//执行object这种方法不需要远程调用，执行本地方法即可
		int clientHashCode = service.hashCode();
		System.out.println(clientHashCode);
	}
	
}
