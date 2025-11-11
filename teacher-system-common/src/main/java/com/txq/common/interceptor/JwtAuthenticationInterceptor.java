package com.txq.common.interceptor;

import com.txq.common.context.UserContext;
import com.txq.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sjh.jwt.JwtUtils;
import sjh.jwt.KeyUtils;

import java.security.PublicKey;
import java.util.List;

/**
 * JWT认证拦截器
 * 从请求头中解析JWT token，提取用户信息并存入上下文
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    // 公钥Base64字符串，从配置中心注入
    @Value("${keypair.public-key}")
    private String publicKeyString;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");

        // 如果没有token，直接放行（由权限注解控制是否需要登录）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        try {
            // 提取 token
            String token = authHeader.substring(7);
            log.info("收到token: {}...", token.substring(0, Math.min(50, token.length())));

            // 解析 token
            PublicKey publicKey = KeyUtils.getPublickey(publicKeyString);

            // 提取用户信息
            String userId = (String) JwtUtils.getClaim(token, publicKey, "sub");  // 用户工号
            log.info("从token中解析出用户ID: {}", userId);

            @SuppressWarnings("unchecked")
            List<Integer> roleIds = (List<Integer>) JwtUtils.getClaim(token, publicKey, "roleId");  // 角色ID列表
            log.info("从token中解析出角色列表: {}", roleIds);

            // 存入上下文
            UserContext.setUserId(userId);
            UserContext.setRoleIds(roleIds);

            log.info("JWT认证成功，用户ID: {}, 角色: {}", userId, roleIds);

            return true;
        } catch (Exception e) {
            log.error("JWT解析失败", e);
            throw new BizException(401, "无效的token");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除上下文
        UserContext.clear();
    }
}

