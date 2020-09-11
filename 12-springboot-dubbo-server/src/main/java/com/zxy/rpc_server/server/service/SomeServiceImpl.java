package com.zxy.rpc_server.server.service;

import org.springframework.stereotype.Service;

import com.zxy.rpc_api.service.SomeService;

@Service
public class SomeServiceImpl implements SomeService{

	public String hello(String name) {
		// TODO Auto-generated method stub
		return name.replaceAll("delete", "DEL");
	}

}
