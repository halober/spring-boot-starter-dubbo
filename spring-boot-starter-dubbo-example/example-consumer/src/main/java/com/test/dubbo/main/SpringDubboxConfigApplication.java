package com.test.dubbo.main;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class SpringDubboxConfigApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringDubboxConfigApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.err.println("服务消费者启动完毕------>>启动完毕");
	}
	
}
