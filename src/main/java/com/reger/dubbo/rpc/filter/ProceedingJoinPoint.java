package com.reger.dubbo.rpc.filter;

import java.util.List;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;

public class ProceedingJoinPoint {
	private final Invoker<?> invoker;
	private final Invocation invocation;
	private final List<? extends RpcFilter> rpcFilters;
	private volatile int index = 0;
	private volatile int rpcFilterSize = 0;

	protected ProceedingJoinPoint(Invoker<?> invoker, Invocation invocation, List<? extends RpcFilter> rpcFilters) {
		super();
		this.invoker = invoker;
		this.invocation = invocation;
		this.rpcFilters = rpcFilters;
		if (this.rpcFilters != null) {
			this.rpcFilterSize = this.rpcFilters.size();
		}
	}

	public Result proceed() {
		if (index >= rpcFilterSize) {
			return invoker.invoke(invocation);
		} else {
			RpcFilter rpcFilter = rpcFilters.get(index++);
			return rpcFilter.invoke(this);
		}
	}

	public Invoker<?> getInvoker() {
		return invoker;
	}

	public Invocation getInvocation() {
		return invocation;
	}
}
