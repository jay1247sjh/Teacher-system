package com.txq.common.aspect;

import com.txq.common.annotation.RequiresRole;
import com.txq.common.context.UserContext;
import com.txq.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 角色权限校验切面
 */
@Slf4j
@Aspect
@Component
@Order(10)  // 确保在JWT拦截器之后执行
public class RoleAuthorizationAspect {

    @Around("@annotation(com.txq.common.annotation.RequiresRole) || @within(com.txq.common.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);
        if (requiresRole == null) {
            // 如果方法上没有，尝试获取类上的注解
            requiresRole = joinPoint.getTarget().getClass().getAnnotation(RequiresRole.class);
        }

        if (requiresRole == null) {
            // 没有注解，直接放行
            return joinPoint.proceed();
        }

        // 获取当前用户的角色ID
        List<Integer> userRoleIds = UserContext.getRoleIds();
        if (userRoleIds == null || userRoleIds.isEmpty()) {
            log.warn("用户未登录或token无效，拒绝访问: {}", method.getName());
            throw new BizException(401, "请先登录");
        }

        // 获取需要的角色
        int[] requiredRoles = requiresRole.value();
        boolean requireAll = requiresRole.requireAll();

        // 校验权限
        boolean hasPermission;
        if (requireAll) {
            // 必须拥有所有角色
            hasPermission = Arrays.stream(requiredRoles)
                    .allMatch(userRoleIds::contains);
        } else {
            // 拥有任意一个角色即可
            hasPermission = Arrays.stream(requiredRoles)
                    .anyMatch(userRoleIds::contains);
        }

        if (!hasPermission) {
            log.warn("用户角色不足，拒绝访问: {} 需要角色: {}, 用户角色: {}",
                    method.getName(), Arrays.toString(requiredRoles), userRoleIds);
            throw new BizException(403, "权限不足，无法执行该操作");
        }

        log.debug("权限校验通过，用户角色: {}, 需要角色: {}", userRoleIds, Arrays.toString(requiredRoles));

        // 权限校验通过，执行方法
        return joinPoint.proceed();
    }
}

