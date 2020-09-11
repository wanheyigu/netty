package com.zxy.rpc_server;

import com.zxy.rpc_server.registry.RegistryCenter;
import com.zxy.rpc_server.registry.impl.ZKRegistryCenterImpl;

public class RegistryTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		RegistryCenter registryCenter = new ZKRegistryCenterImpl();
		
		registryCenter.register("com.zxy.rpc.TestService", "localhost:8888");
	
		//保证可以看到结果，应为以上注册完成后，临时会话就结束了
		System.in.read();
	}

}
