package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.rpc.Result;

public interface ConsumerFilter{

	Result invoke(ProceedingJoinPoint point);
}