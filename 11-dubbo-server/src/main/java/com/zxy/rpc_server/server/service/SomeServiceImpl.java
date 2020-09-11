package com.zxy.rpc_server.server.service;

import com.zxy.rpc_api.service.SomeService;

public class SomeServiceImpl implements SomeService{

	public String hello(String name) {
		// TODO Auto-generated method stub
		return name.replaceAll("delete", "DEL");
	}

}
