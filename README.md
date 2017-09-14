# spring-boot-starter-dubbo


spring-boot-start-dubbo，是spring-boot与dubbo有机结合的桥梁。

根据`spring-boot`开箱即用的原则实现的spring-boot-start-dubbo，dubbo的使用变得及其简单快捷，容易上。让你从dubbo小白到能正常使用，只需一盏茶的功夫。

使用本项目，你肯定会发现，原来`dubbo`开发如此简单。

### 本项目特点
###### 1.支持dubbo原生所有的配置项，使用spring-boot方式配置
###### 2.配置项描述清晰，让你在配置参数时，等同在看dubbo官方中文文档（需要安装spring-ide插件）
###### 3.提供注解@Inject，用来替换@Reference的依赖注入，让spring+dubbo时依赖注入注解更简单（该注解如果不能从spring上下文注入对象，将使用等同@Reference的依赖注入方式注入对象）

### 简单示例
1.示例项目推荐使用zookeeper作为注册中心，因为线上你肯定会用它。如果你本地没有可用的zookeeper服务，你可以[点击这里下载](http://mirror.bit.edu.cn/apache/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz),下载后解压，进入zookeeper的conf目录，拷贝zoo_sample.cfg它为zoo.cfg，进入zookeeper的bin目录,windows系统下双击zkServer.cmd，linux下执行zkServer.sh命令。

2.下载示例的服务发布者[example-provider](http://central.maven.org/maven2/com/gitee/reger/example-provider/1.0.1/example-provider-1.0.1.jar)，执行命令
```sh
java -jar example-provider-1.0.1.jar --spring.dubbo.address=127.0.0.1  --spring.dubbo.port=2181 
```
3.下载示例的服务使用者[example-consumer](http://central.maven.org/maven2/com/gitee/reger/example-consumer/1.0.1/example-consumer-1.0.1.jar)，执行命令
```sh
java -jar example-consumer-1.0.1.jar --spring.dubbo.address=127.0.0.1  --spring.dubbo.port=2181 
```
4.consumer和provider都有正常调用的日志输出，至此简单示例就运行起来了，也表示你本地的dubbo环境已经可以用了。

5.克隆示例代码，开始`dubbo`之旅。
```sh
git clone https://gitee.com/reger/spring-boot-starter-dubbo.git
```  
## 快速入门
#### 1.在maven管理的spring-boot项目中引入依赖,（建议使用spring-boot版本1.5以上,1.5以下未测试过）
```xml
    <dependency>
        <groupId>com.gitee.reger</groupId>
        <artifactId>spring-boot-starter-dubbo</artifactId>
        <version>${spring-boot-starter-dubbo.version}</version>
    </dependency>
 ```
#### 2.在spring-boot项目的配置文件'application.yml'中增加dubbo的配置项
###### 服务提供者增加
```yml
spring:
  dubbo: 
    application:
      name: demo-provider
    base-package: com.test.dubbo.provider  # dubbo服务发布者所在的包
    registry:
      address: 127.0.0.1                   # zookeeper注册中心的地址
      port: 2181                           # zookeeper注册中心的端口
    protocol:
      name: dubbo
      serialization: hessian2
    provider:
      retries: 0                           # 服务调用重试次数，服务发布者不给重试，让服务调用者自己重试
```
###### 服务调用者增加
```yml
spring:
  dubbo: 
    application:
      name: demo-consumer
    base-package: com.test.dubbo.consumer  # dubbo服务调用者所在的包  
    registry:
      address: 127.0.0.1                   # zookeeper注册中心的地址
      port: 2181                           # zookeeper注册中心的端口
    consumer:
      timeout: 1000 
      check: true                          # 服务启动时检查被调用服务是否可用
      retries: 2                           # 服务调用重试次数 
```
#### 3. 定义服务接口，
在api项目中增加接口
```java
package com.test.dubbo.service;

public interface DemoService {
    Integer add(Integer a,Integer b);
}
```
#### 4. 服务发布
服务提供者项目中增加业务类
```java
package com.test.dubbo.provider;
import com.test.dubbo.service.DemoService;
import com.alibaba.dubbo.config.annotation.Service;

@Service
public class DemoServiceImpl implements DemoService{

    public Integer add(Integer a,Integer b){
        System.err.printf("方法add被调用 %s+%s", a, b);
        System.err.println();
        if(a==null||b==null){
            return 0;
        }
        return a+b;
    }
}
```
#### 5. 服务调用者
服务调用者项目中增加业务类
```java
package com.test.dubbo.consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.config.annotation.Reference;
import com.reger.dubbo.annotation.Inject;

import com.test.dubbo.service.DemoService;

@Component
public class DemoConsumer implements CommandLineRunner {

    // 使用dubbo原生注入，可以选择使用@Inject兼容注入
    @Reference DemoService service; 

    @Override
    public void run(String... args){  
        int a=1;
        int b =2;
        System.err.printf("%s+%s=%s", a, b, service.add(a,b));
        System.err.println(); 
    }
}
```
#### 6.启动服务提供者，启动服务调用者。
服务提供者spring-boot的main方法的示例
```java
package com.test.dubbo.main;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class SpringDubboConfigApplication implements CommandLineRunner {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SpringDubboxConfigApplication.class, args);
        TimeUnit.MINUTES.sleep(10); //提供者main线程暂停10分钟等待被调用
        System.err.println("服务提供者------>>服务关闭");
    }

    @Override
    public void run(String... args) throws Exception {
        System.err.println("服务提供者------>>启动完毕");
    } 
}
```
服务调用者spring-boot的main方法的类示例
```java
package com.test.dubbo.main;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication 
public class SpringDubboConfigApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringDubboxConfigApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.err.println("服务调用者------>>启动完毕");
    }
}
```
 