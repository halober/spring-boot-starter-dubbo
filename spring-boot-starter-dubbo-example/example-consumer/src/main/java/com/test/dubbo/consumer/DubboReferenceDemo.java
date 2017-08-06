/*
 * Copyright 2006-2014 handu.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.dubbo.consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.test.dubbo.model.User;
import com.test.dubbo.service.MathService;

/**
 * @author Jinkai.Ma
 */
@Component
public class DubboReferenceDemo implements CommandLineRunner {

	@Reference
	private MathService bidService;

	Integer a=1;
	Integer b=2;
	Integer c=3;

	Integer d=2222;
	Integer e=2223;
	Integer f=2224;

	@Override
	public void run(String... args) throws Exception {
		
		System.err.println(a+" "+b+" "+c+" "+d+" "+e+" "+f);

		System.err.println(a + "+" + b + "=" + bidService.add(a, b));
		System.err.println(a + "+" + b + "=" + bidService.add(a, c));
		System.err.println(a + "+" + b + "=" + bidService.add(a, d));
		System.err.println(a + "+" + b + "=" + bidService.add(a, e));
		System.err.println(a + "+" + b + "=" + bidService.add(a, f));
		System.err.println(a + "+" + b + "=" + bidService.add(e, b));
		System.err.println(a + "+" + b + "=" + bidService.add(f, b));

		System.err.println(a + "+" + b + "=" + bidService.add(a, b));
		System.err.println(a + "+" + b + "=" + bidService.add(a, c));
		System.err.println(a + "+" + b + "=" + bidService.add(a, d));
		System.err.println(a + "+" + b + "=" + bidService.add(a, e));
		System.err.println(a + "+" + b + "=" + bidService.add(a, f));
		System.err.println(a + "+" + b + "=" + bidService.add(e, b));
		System.err.println(a + "+" + b + "=" + bidService.add(f, b));

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
//		this.add(11, bidService.add(a, b));
		// bidService.throwThrowable();
	}


}
