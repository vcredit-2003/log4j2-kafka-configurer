package com.vcredit.log;

import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Dong Zhuming
 */
public class KafkaLogStartUpListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private static final String TRUE = "true";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {

        final ConfigurableEnvironment environment = event.getEnvironment();

        final String kafkaServers = findKafkaConfig(environment);
        final String logToKafka = environment.getProperty("logToKafka");
        if (!"".equals(kafkaServers) && TRUE.equals(logToKafka)) {
            System.out.println(String.format("发现kafka配置[%s]", kafkaServers));
            MDC.put("kafka-log-enabled", logToKafka);
            MDC.put("kafka-servers", kafkaServers);
            MDC.put("application-name", nullToEmpty(environment.getProperty("spring.application.name")));
            MDC.put("profile", String.join(",", environment.getActiveProfiles()));
            try {
                MDC.put("currentIp", InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            MDC.put("kafka-log-enabled", "false");
        }
    }

    private String findKafkaConfig(ConfigurableEnvironment environment) {
        return nullToEmpty(environment.getProperty("spring.kafka.bootstrap-servers"));
    }

    private String nullToEmpty(String property) {
        return property == null ? "" : property;
    }

    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER - 1;
    }
}
