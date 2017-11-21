package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Node;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;

public interface JoinPoint<T> extends Node, Invocation {

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
}
