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
#### 2.监控中心配置参数（可选配置）
```yml
spring:
  dubbo:
    monitor:                # 监控服务
      address: N/A          # 可选 服务治理 直连监控中心服务器地址，address="10.20.130.230:12080" 1.0.16以上版本
      protocol: dubbo       # 可选 服务治理 监控中心协议，如果为protocol="registry"，表示从注册中心发现监控中心地址，否则直连监控中心。 2.0.9以上版本
```
#### 3.模块定义（可选配置）
```yml
spring:
  dubbo:
    module:                 # 应用模块定义
      name:                 # 必填 服务治理 当前模块名称，用于注册中心计算模块间依赖关系 2.2.0以上版本
      organization:         # 可选 服务治理 组织名称(BU或部门)，用于注册中心区分服务来源，此配置项建议不要使用autoconfig，直接写死在配置中，比如china,intl,itu,crm,asc,dw,aliexpress等 2.2.0以上版本
      owner:                # 可选 服务治理 模块负责人，用于服务治理，请填写负责人公司邮箱前缀 2.2.0以上版本
      version:              # 可选 服务治理 当前模块的版本 2.2.0以上版本
```
#### 4.服务调用支持的类型（必须配置）
```yml

spring:
  dubbo:
    protocol:               # 默认的应用协议栈
      name: dubbo           # 必填 性能调优 协议名称 2.0.5以上版本
      serialization: hessian2 #可选 性能调优 协议序列化方式，当协议支持多种序列化方式时使用，比如：dubbo协议的dubbo,hessian2,java,compactedjava，以及http协议的json等 2.0.5以上版本
      accepts: 0            # 可选 性能调优 服务提供方最大可接受连接数 2.0.5以上版本
      accesslog: false      # 可选 服务治理 设为true，将向logger中输出访问日志，也可填写访问日志文件路径，直接把访问日志输出到指定文件 2.0.5以上版本
      buffer: 8192          # 可选 性能调优 网络读写缓冲区大小 2.0.5以上版本
      charset: UTF-8        # 可选 性能调优 序列化编码 2.0.5以上版本
      client: netty         # 可选 性能调优 协议的客户端实现类型，比如：dubbo协议的mina,netty等 2.0.5以上版本
      codec: dubbo          # 可选 性能调优 协议编码方式 2.0.5以上版本
      contextpath:          # 可选 服务治理 2.0.6以上版本
      dispatcher: all       # 可选 性能调优 协议的消息派发方式，用于指定线程模型，比如：dubbo协议的all, direct, message, execution, connection等 2.1.0以上版本
      heartbeat: 0          # 可选 性能调优 心跳间隔，对于长连接，当物理层断开时，比如拔网线，TCP的FIN消息来不及发送，对方收不到断开事件，此时需要心跳来帮助检查连接是否已断开 2.0.10以上版本
      host:                 # 可选 服务发现 -服务主机名，多网卡选择或指定VIP及域名时使用，为空则自动查找本机IP，-建议不要配置，让Dubbo自动获取本机IP 2.0.5以上版本 
      id: dubbo             # 可选 配置关联 协议BeanId，可以在<dubbo:service protocol="">中引用此ID，如果ID不填，缺省和name属性值一样，重复则在name后加序号。 2.0.5以上版本
      iothreads:            # 可选 性能调优 io线程池大小(固定大小) 2.0.5以上版本 
      path:                 # 可选 服务发现 提供者上下文路径，为服务path的前缀 2.0.5以上版本
      payload: 88388608     # 可选 性能调优 请求及响应数据包大小限制，单位：字节 2.0.5以上版本
      port:                 # 可选 服务发现 不输入或者输入0，将自动在53600~53688之间生产一个  服务端口  2.0.5以上版本 ， 
      queues: 0             # 可选 性能调优 线程池队列大小，当线程池满时，排队等待执行的队列大小，建议不要设置，当线程程池时应立即失败，重试其它服务提供机器，而不是排队，除非有特殊需求。 2.0.5以上版本
      register: true        # 可选 服务治理 该协议的服务是否注册到注册中心 2.0.8以上版本
      server:               # 可选 性能调优 协议的服务器端实现类型，比如：dubbo协议的mina,netty等，http协议的jetty,servlet等 2.0.5以上版本
      telnet:               # 可选 服务治理 所支持的telnet命令，多个命令用逗号分隔 2.0.5以上版本
      threadpool: fixed     # 可选 性能调优 线程池类型，可选：fixed/cached 2.0.5以上版本
      threads: 100          # 可选 性能调优 服务线程池大小(固定大小) 2.0.5以上版本
      transporter: netty    # 可选 性能调优 协议的服务端和客户端实现类型，比如：dubbo协议的mina,netty等，可以分拆为server和client配置 2.0.5以上版本
      
#  如果需要配置多个协议可以使用如下方式
#    protocols:
#      - name: dubbo
#        serialization: nativejava
#      - name: dubbo
#        serialization: hessian2
#      - name: dubbo
#        serialization: fastjson
#      - name: dubbo
#        serialization: dubbo
#      - name: rmi
#      - name: http
#      - name: hessian
#      - name: thrift
#      - name: webservice
```
#### 4.应用配置参数
```yml
spring:
  dubbo: 
    application:
      name: demo-provider     # 必填 服务治理 当前应用名称，用于注册中心计算应用间依赖关系，注意：消费者和提供者应用名不要一样，此参数不是匹配条件，你当前项目叫什么名字就填什么，和提供者消费者角色无关，比如：kylin应用调用了morgan应用的服务，则kylin项目配成kylin，morgan项目配成morgan，可能kylin也提供其它服务给别人使用，但kylin项目永远配成kylin，这样注册中心将显示kylin依赖于morgan 1.0.16以上版本
      owner: laolei           # 可选 服务治理 应用负责人，用于服务治理，请填写负责人公司邮箱前缀 2.0.5以上版本
      architecture:           # 可选 服务治理 用于服务分层对应的架构。如，intl、china。不同的架构使用不同的分层。 2.0.7以上版本
      compiler: javassist     # 可选 性能优化 Java字节码编译器，用于动态类的生成，可选：jdk或javassist 2.1.0以上版本
      environment:            # 可选 服务治理 应用环境，如：develop/test/product，不同环境使用不同的缺省值，以及作为只用于开发测试功能的限制条件 2.0.0以上版本
      logger: slf4j           # 可选 性能优化 日志输出方式，可选：slf4j,jcl,log4j,jdk 2.2.0以上版本
      organization:           # 可选 服务治理 组织名称(BU或部门)，用于注册中心区分服务来源，此配置项建议不要使用autoconfig，直接写死在配置中，比如china,intl,itu,crm,asc,dw,aliexpress等 2.0.0以上版本
      version:                # 可选 服务治理 当前应用的版本 2.2.0以上版本
```