package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.rpc.Result;

public interface RpcFilter{

	Result invoke(ProceedingJoinPoint point);
}