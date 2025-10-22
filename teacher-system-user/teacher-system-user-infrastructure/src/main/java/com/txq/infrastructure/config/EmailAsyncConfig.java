package com.txq.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 邮箱线程池配置类
 */
@Configuration
@EnableAsync
public class EmailAsyncConfig {

    @Bean("mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数为2
        executor.setCorePoolSize(2);
        // 设置最大线程数为5
        executor.setMaxPoolSize(5);
        // 设置队列容量为50
        executor.setQueueCapacity(50);
        // 设置线程名前缀
        executor.setThreadNamePrefix("mail-exec-");
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
