package com.reger.dubbo.rpc.filter;

import java.util.Map;

import com.alibaba.dubbo.common.Node;
import com.alibaba.dubbo.rpc.Result;

public interface JoinPoint<T> extends Node {

	/**
	 * 调用真实方法
	 * 
	 * @return
	 */
	Result proceed();

	/**
	 * 接口信息
	 */
	Class<T> getInterface();
	
    /**
     * 方法名
     * @return
     */
    String getMethodName();

    /**
     * 参数类型
     * @return
     */
    Class<?>[] getParameterTypes();

    /**
     * 参数名
     * @return
     */
    Object[] getArguments();

    /**
     * 隐式传参
     * @return
     */
    Map<String, String> getAttachments();

    /**
     * 获取隐式参数
     * @param key 参数名
     * @return
     */
    String getAttachment(String key);

    /**
     * 获取隐式参数
     * @param key 参数名
     * @param defaultValue 参数默认值
     * @return 
     */
    String getAttachment(String key, String defaultValue);
}
