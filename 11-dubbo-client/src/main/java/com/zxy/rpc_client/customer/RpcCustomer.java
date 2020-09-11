package com.zxy.rpc_client.customer;

import com.zxy.rpc_api.service.SomeService;
import com.zxy.rpc_client.client.RpcProxy;

public class RpcCustomer {

	public static void main(String[] args) {
		// 通过本地代理获取接口实例
		SomeService service = RpcProxy.creat(SomeService.class);
		if(service != null) {
			String result = service.hello("the delete never to run!");
			
			System.out.println(result);
		}
	}

}
