package com.txq.common.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Prometheus配置类
 */
@Configuration
public class PrometheusConfiguration {

    // 不要自定义PrometheusMeterRegistry类，否则SpringBoot项目不会再创建自带的
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment env) {
        return registry -> registry.config().commonTags(
                "application", env.getProperty("spring.application.name", "unknown"),
                "env", env.getProperty("spring.profiles.active", "unknown")
        );
    }
}