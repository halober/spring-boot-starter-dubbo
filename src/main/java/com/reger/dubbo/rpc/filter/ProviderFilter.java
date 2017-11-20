package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;

public interface ProviderFilter {

	Result invoke(Invoker<?> invoker, Invocation invocation);

}