package com.test.dubbo.consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.reger.dubbo.annotation.Inject;
import com.test.dubbo.model.User;
import com.test.dubbo.service.MathService;

@Component
public class DubboReferenceConsumer implements CommandLineRunner {

	@Reference
	public MathService service;
	@Inject
	public MathService bidService;

	Integer a=1;
	Integer b=2;
	Integer c=3;

	Integer d=2222;
	Integer e=2223;
	Integer f=2224;

	@Override
	public void run(String... args) throws Exception {
		System.err.println(a+" "+b+" "+c+" "+d+" "+e+" "+f);

		System.err.println("注入的是同一个对象："+bidService.equals(service));
		System.err.println(a + "+" + b + "=" + bidService.add(a, b));
		System.err.println(a + "+" + b + "=" + bidService.add(a, c));
		System.err.println(a + "+" + b + "=" + bidService.add(a, d));
		System.err.println(a + "+" + b + "=" + bidService.add(a, e));
		System.err.println(a + "+" + b + "=" + bidService.add(a, f));
		System.err.println(a + "+" + b + "=" + bidService.add(e, b));
		System.err.println(a + "+" + b + "=" + bidService.add(f, b));
		System.err.println("list=" + bidService.toList(1, "22", true, 'b'));
		User user = bidService.getUser(new User(11,"张三","张三的密码"));
		System.err.println(user);
		try {
			bidService.throwThrowable();
		} catch (Throwable e) {
			System.err.println(e.getMessage());
		}
	}


}
