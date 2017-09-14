# spring-boot-starter-dubbo

spring-boot-start-dubbo，是spring-boot与dubbo有机结合的桥梁，根据`spring-boot`开箱即用的原则实现，使dubbo的使用变得及其简单快捷，容易上手。让dubbo小白正常使用dubbo，只需一盏茶的功夫。

使用本项目，你肯定会发现，原来`dubbo发布服务如此简单`。

### 本项目特点
###### 1.支持dubbo原生所有的配置项，使用spring-boot方式配置
###### 2.配置项描述清晰，让你在配置参数时，`等同在看dubbo官方中文文档`（需要安装spring-ide插件）
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
#### 4. 服务提供者
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
## 可用配置项  
#### 1.注册中心支持的配置参数 （必须配置）
```yml
spring:
  dubbo:
    registry:               # 应用注册中心配置项
      protocol: zookeeper   # 必填 服务发现 注册中心支持的协议 包括 dubbo,multicast,zookeeper,redis 默认是zookeeper
      address: 127.0.0.1    # 必填 服务发现 注册中心服务器地址，如果地址没有端口缺省为9090，同一集群内的多个地址用逗号分隔，如：ip:port,ip:port，不同集群的注册中心，请配置多个spring.dubbo.registry.标签 1.0.16以上版本
      port: 2181            # 可选 服务发现 注册中心缺省端口，当address没有带端口时使用此端口做为缺省值 2.0.0以上版本
      client: zkclient      # 可选 服务发现 注册中心支持的客户端， zookeeper 支持客户端包括 curator和zkclient,如果不配置，默认使用zkclient 
      session: 60000        # 可选 性能调优 注册中心会话超时时间(毫秒)，用于检测提供者非正常断线后的脏数据，比如用心跳检测的实现，此时间就是心跳间隔，不同注册中心实现不一样。 2.1.0以上版本
      register: true        # 可选 服务治理 是否向此注册中心注册服务，如果设为false，将只订阅，不注册 2.0.5以上版本
      check: false          # 可选 服务治理 服务是否动态注册，如果设为false，注册后将显示后disable状态，需人工启用，并且服务提供者停止时，也不会自动取消册，需人工禁用。 2.0.5以上版本
      dynamic: true         # 可选 服务治理 服务是否动态注册，如果设为false，注册后将显示后disable状态，需人工启用，并且服务提供者停止时，也不会自动取消册，需人工禁用。 2.0.5以上版本
      file: regcache.log    # 可选 服务治理 使用文件缓存注册中心地址列表及服务提供者列表，应用重启时将基于此文件恢复，注意：两个注册中心不能使用同一文件存储 2.0.0以上版本
      username:             # 可选 服务治理 登录注册中心用户名，如果注册中心不需要验证可不填 2.0.0以上版本
      password:             # 可选 服务治理 登录注册中心密码，如果注册中心不需要验证可不填 2.0.0以上版本
      subscribe:            # 可选 服务治理 是否向此注册中心订阅服务，如果设为false，将只注册，不订阅 2.0.5以上版本
      timeout:              # 可选 性能调优 注册中心请求超时时间(毫秒) 2.0.0以上版本
      wait: 0               # 可选 性能调优 停止时等待通知完成时间(毫秒) 2.0.0以上版本
      transport: netty      # 可选 性能调优 网络传输方式，可选mina,netty 2.0.0以上版本
      id:                   # 可选 配置关联 注册中心引用BeanId，可以在<dubbo:service registry="">或<dubbo:reference registry="">中引用此ID 1.0.16以上版本
```
#### 2.监控中心配置参数（非必需配置）
```yml
spring:
  dubbo:
    monitor:                # 监控服务
      address: N/A          # 可选 服务治理 直连监控中心服务器地址，address="10.20.130.230:12080" 1.0.16以上版本
      protocol: dubbo       # 可选 服务治理 监控中心协议，如果为protocol="registry"，表示从注册中心发现监控中心地址，否则直连监控中心。 2.0.9以上版本
```
