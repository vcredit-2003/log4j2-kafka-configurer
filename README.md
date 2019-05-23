## 背景
* Spring boot默认使用了logback作为日志输出框架，但logback未集成kafka输出组件
* 网上的解决方案存在一定瑕疵，如Kafka宕机时，应用无法正常启动
* 将Spring Boot的默认日志组件改成Log4j后，虽然可以使用，但无法集成Spring Boot的环境变量
* 因此开发一个基于Log4j的Kafka日志输出框架
  
## 使用方法
* 在pom.xml中使用以下方式加入依赖
```
    <dependency>
        <groupId>com.vcredit.framework</groupId>
        <artifactId>log4j2-kafka-configurer</artifactId>
        <version>1.0.0-RELEASE</version>
    </dependency>
```
* 在项目中的application.yml中增加 `logToKafka: true`，表示应用启动后会主动装载Kafka的相关配置
* 确保`spring.kafka.bootstrap-servers`配置项下包含了正确的Kafka服务器连接信息
* 框架中包含了log4j2-spring.xml.sample文件，在应用时可以直接替换成log4j2-spring.xml放在resources目录下，其中选项PROJECT_NAME决定了日志输出到kafka的topic名称

## 提示
* 启动时，若应用内有效配置了kafka的连接参数，会显示`发现kafka配置[...]`字样，如没有出现，请检查配置