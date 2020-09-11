package com.zxy.rpc_server.server;

import com.zxy.rpc_server.registry.RegistryCenter;
import com.zxy.rpc_server.registry.impl.ZKRegistryCenterImpl;

public class RpcServerStart {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RpcServer rpcServer = new RpcServer();
		
		RegistryCenter registryCenter = new ZKRegistryCenterImpl();
		String serviceAddress = "localhost:8001";
		
		
		rpcServer.publishService(registryCenter,serviceAddress,"com.zxy.rpc_server.server.service");
		
		rpcServer.starter();

	}

}
