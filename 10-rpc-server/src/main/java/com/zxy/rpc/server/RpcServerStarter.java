package com.zxy.rpc.server;

public class RpcServerStarter {

	public static void main(String[] args) throws Exception {
		RpcServer server = new RpcServer();
		
		//将指定包下的服务提供者发布到服务器
		server.publish("com.zxy.rpc.service");
		
		//启动服务器
		server.start();
	}

}
