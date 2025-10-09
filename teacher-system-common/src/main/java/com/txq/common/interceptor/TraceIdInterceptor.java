package com.txq.common.interceptor;

import com.txq.common.status.TraceIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 链路追踪拦截器
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头获取traceId
        String traceId = request.getHeader("X-Trace-Id");
        // 如果没有则生成新的traceId
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        TraceIdHolder.setTraceId(traceId);
        // 在请求头设置链路ID
        response.setHeader("X-Trace-Id", traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TraceIdHolder.clear();
    }
}
