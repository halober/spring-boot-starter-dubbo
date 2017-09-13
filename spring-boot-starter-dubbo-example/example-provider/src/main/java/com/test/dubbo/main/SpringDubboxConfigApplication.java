package com.test.dubbo.main;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class SpringDubboxConfigApplication implements CommandLineRunner {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SpringDubboxConfigApplication.class, args);
		TimeUnit.MINUTES.sleep(10); //发布者暂停10分钟等待被调用
		System.err.println("服务提供者等待结束------>>服务关闭");
	}

	@Override
	public void run(String... args) throws Exception {
		System.err.println("服务提供者启动完毕------>>启动完毕");
	}
	
}
