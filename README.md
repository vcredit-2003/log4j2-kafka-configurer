This is tool for log4j2 appender to read kafka configuration if you want to output your logs by log4j2 in Spring boot applications. 

## Backgrounds
* Spring boot introduces logback as its default logging framework, but no Kafka appender integrated within logback 
* A simple solution is to use https://github.com/danielwegener/logback-kafka-appender
* If you really want to switch logback to log4j2, you can try with this spring-boot based configurer and benefit the advantages from log4j2
  
## Usages
* Download the project source code and `mvn install` to install it
* Add dependency into your pom.xml  
```xml
  <dependency>
    <groupId>com.vcredit.framework</groupId>
    <artifactId>log4j2-kafka-configurer</artifactId>
    <version>1.0.0-RELEASE</version>
  </dependency>
```
* add `logToKafka: true` to application.yml, which indicates properties related to Kafka which be loaded
* ensure the kafka properties are correctly configured in `spring.kafka.bootstrap-servers`
* log4j2-spring.xml.sample is included in the source code project, you can just copy it into your resources directory and rename to log4j2-spring.xml
${PROJECT_NAME} is the topic name of Kafka

## Tips
* When starting application, you will see `Kafka configuration is discovered` which means the framework will try to connect kafka for outputting logs. If not see, please check application.yml 