package com.zxy.rpc_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.zxy.rpc_server.registry.RegistryCenter;
import com.zxy.rpc_server.server.RpcServer;

/**
 * 需要实现CommandLineRunner接口，在启动的时候会自动调用run方法
 */
@SpringBootApplication
public class RpcServerStart implements CommandLineRunner{
	
	@Autowired
	private RpcServer rpcServer;
	@Autowired
	private RegistryCenter registryCenter;
	
	public static void main(String[] args){
		
		SpringApplication.run(RpcServerStart.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		rpcServer.publishService(registryCenter,"localhost:8001","com.zxy.rpc_server.server.service");
		rpcServer.starter();
		
	}
	
}
