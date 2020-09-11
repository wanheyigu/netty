package com.zxy.rpc_server.server.service.check;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.zxy.rpc_api.service.check.CheckServer;

@Service
public class CheckServerServiceImpl implements CheckServer{

	public Date getServerTime() {
		// TODO Auto-generated method stub
		return new Date();
	}

}
