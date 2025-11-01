package com.txq.common.configuration;

import com.txq.common.interceptor.JwtAuthenticationInterceptor;
import com.txq.common.interceptor.TraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final TraceIdInterceptor traceIdInterceptor;
    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    
    @Value("${attachment.base-path}")
    private String attachmentBasePath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 禁用后缀匹配，避免 /table/123/fields 被当作静态资源
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 只处理明确的静态资源路径，不处理 /table/** 等 API 路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
        
        // 配置附件访问路径（用户头像等）
        // 访问 /attachments/** 时，映射到本地文件系统
        // 路径从环境变量 ATTACHMENT_BASE_PATH 读取
        String fileLocation = "file:" + attachmentBasePath;
        if (!fileLocation.endsWith("/")) {
            fileLocation += "/";
        }
        registry.addResourceHandler("/attachments/**")
                .addResourceLocations(fileLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. TraceId拦截器（最先执行）
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 2. JWT认证拦截器（第二执行）
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",      // 登录接口
                        "/user/register",   // 注册接口
                        "/user/send-code",  // 发送验证码接口
                        "/actuator/**",     // 监控端点
                        "/error",           // 错误页面
                        "/attachments/**"   // 附件访问（静态资源，无需认证）
                )
                .order(2);
    }
}

