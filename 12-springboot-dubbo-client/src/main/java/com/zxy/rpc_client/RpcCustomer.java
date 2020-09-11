package com.zxy.rpc_client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.zxy.rpc_api.service.SomeService;
import com.zxy.rpc_client.client.RpcProxy;


@SpringBootApplication
public class RpcCustomer implements CommandLineRunner{

	@Autowired
	private RpcProxy proxy;
	
	public static void main(String[] args) {
		SpringApplication.run(RpcCustomer.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {

		SomeService service = proxy.creat(SomeService.class);
		if(service != null) {
			String result = service.hello("the delete never to run!");
			
			System.out.println(result);
		}
		
	}

}
