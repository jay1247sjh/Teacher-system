package com.txq.common.configuration;

import com.txq.common.interceptor.JwtAuthenticationInterceptor;
import com.txq.common.interceptor.TraceIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final TraceIdInterceptor traceIdInterceptor;
    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

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
                        "/error"            // 错误页面
                )
                .order(2);
    }
}

