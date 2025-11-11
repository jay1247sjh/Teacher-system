package com.txq.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 邮箱配置类
 * */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    // 发件人
    private String from;

    // 标题
    private Subject subject;

    // 模版
    private Template template;

    @Data
    public static class Subject {
        private String title;
    }

    @Data
    public static class Template {
        private String description;
    }


}
