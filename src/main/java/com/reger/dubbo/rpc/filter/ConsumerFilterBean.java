package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group = Constants.CONSUMER)
public class ConsumerFilterBean implements Filter {
	private static ConsumerFilter consumerFilter;

	public static void setConsumerFilter(ConsumerFilter consumerFilter) {
		ConsumerFilterBean.consumerFilter = consumerFilter;
	}

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if(consumerFilter==null){
			return invoker.invoke(invocation);
		}else{
			return consumerFilter.invoke(invoker, invocation);
		}
	}
}