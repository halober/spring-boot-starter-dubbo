package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.rpc.Result;

public interface ProviderFilter {

	Result invoke(ProceedingJoinPoint point);

}