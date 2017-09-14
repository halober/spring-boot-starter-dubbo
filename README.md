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

5.克隆示例代码，开始你的dubbo之旅。
```sh
git clone https://gitee.com/reger/spring-boot-starter-dubbo.git
```  