package com.test.dubbo.provider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.dubbo.config.annotation.Service;
import com.test.dubbo.model.User;
import com.test.dubbo.service.MathService;

@Service(protocol = {  "rmi" } , validation = "true", interfaceClass = MathService.class)
public class MathServiceImpl implements MathService {

	@Override
	public Integer add( Integer a, Integer b) {
		System.err.println("请求到达  " + a + "+" + b + "=" + (a + b));
		return a + b;
	}

	@Override
	public List<Object> toList(Object... args) {
		List<Object> list = new LinkedList<>();
		Collections.addAll(list, args);
		return list;
	}
	@Override
	public void throwThrowable() {
		throw new RuntimeException("专门抛出一个异常试试异常时！");
	}

	@Override
	public User getUser(User user) {
		System.err.println(user);
		return user;
	}
}
