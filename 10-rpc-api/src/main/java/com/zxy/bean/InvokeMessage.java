package com.zxy.bean;

import java.io.Serializable;

import lombok.Data;
/**
 * 远程调用请求参数对象
 */
@Data
public class InvokeMessage implements Serializable{
	/**
	 * 接口名称，即服务名称
	 */
	private String className;
	/**
	 * 远程调用的方法名
	 */
	private String methodName;
	
	/**
	 * 远程调用方法的参数类型列表
	 */
	private Class<?>[] paramTypes;
	/**
	 * 远程调用方法的参数值列表
	 */
	private Object[] paramValues;
}
