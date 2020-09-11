package com.zxy.rpc.service;

import com.zxy.service.SomeService;

public class SomeServiceImpl implements SomeService {

	@Override
	public String hello(String name) {
		// TODO Auto-generated method stub
		return "Hello,"+name+"----->this is RpcInvoke!";
	}

}
