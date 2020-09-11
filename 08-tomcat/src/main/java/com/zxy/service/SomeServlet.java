package com.zxy.service;

import com.zxy.servlet.CustomHttpRequest;
import com.zxy.servlet.CustomHttpResponse;
import com.zxy.servlet.CustomHttpServlet;
/*
 * 业务servlet
 * 需要运行到tomcat中的业务
 */
public class SomeServlet extends CustomHttpServlet{

	@Override
	public void doGet(CustomHttpRequest request, CustomHttpResponse response) throws Exception {
		// 从请求中获取数据
		String uri = request.getUri();
		String path = request.getPath();
		String param = request.getParameter("name");
		String method = request.getMethod();
		
		//格式化
		String content = "method = " + method + "\n" + 
						 "uri = " + uri + "\n" +
						 "path = " + path + "\n" + 
						 "param = " + param;
		//响应
		response.write(content);
	}

	@Override
	public void doPost(CustomHttpRequest request, CustomHttpResponse response) throws Exception {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
