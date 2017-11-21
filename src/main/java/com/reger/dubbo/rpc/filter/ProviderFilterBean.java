package com.reger.dubbo.rpc.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group = Constants.PROVIDER)
public class ProviderFilterBean implements Filter {

	private static ProviderFilter providerFilter;

	public static void setProviderFilter(ProviderFilter providerFilter) {
		ProviderFilterBean.providerFilter = providerFilter;
	}

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result relust;
		if (providerFilter == null) {
			relust = invoker.invoke(invocation);
		} else {
			relust = providerFilter.invoke(invoker, invocation);
		}
		return Utils.encoderException(relust);
	}

}