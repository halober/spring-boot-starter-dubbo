package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;

public class ProceedingJoinPoint {
	private final Invoker<?> invoker;
	private final Invocation invocation;
	private final ProceedingJoinPoint point;
	
	protected ProceedingJoinPoint(Invoker<?> invoker, Invocation invocation, ProceedingJoinPoint point) {
		super();
		this.invoker = invoker;
		this.invocation = invocation;
		this.point = point;
	}
	
	public Result proceed() {
		if(point==null){
			return invoker.invoke(invocation);
		}else{
			return point.proceed();
		}
	}

	public Invoker<?> getInvoker() {
		return invoker;
	}

	public Invocation getInvocation() {
		return invocation;
	}
}
