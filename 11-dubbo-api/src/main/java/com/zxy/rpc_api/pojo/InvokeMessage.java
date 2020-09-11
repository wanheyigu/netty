package com.zxy.rpc_api.pojo;

import java.io.Serializable;

import javax.swing.SingleSelectionModel;

import lombok.Data;
/**
 * 远程调用请求封装类
 */
@Data
public class InvokeMessage implements Serializable{
	
	private String providerName;
	private String methodName;
	private Class<?>[] paramTypes;
	private Object[] paramValues;

}
