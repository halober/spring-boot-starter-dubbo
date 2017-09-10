package com.test.dubbo.main;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class SpringDubboxConfigApplication implements CommandLineRunner {

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SpringDubboxConfigApplication.class, args);
		Thread.currentThread().sleep(1000*60*10); //发布者等待10分钟等待被调用
	}

	@Override
	public void run(String... args) throws Exception {
		System.err.println("服务提供者启动完毕------>>启动完毕");
	}
	
}
