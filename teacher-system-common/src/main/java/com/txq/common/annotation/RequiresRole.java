package com.txq.common.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 标记在需要特定角色才能访问的方法上
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {

    /**
     * 需要的角色ID
     * 用户必须拥有其中任意一个角色才能访问
     */
    int[] value();

    /**
     * 是否需要拥有所有指定的角色
     * true: 必须拥有所有角色（AND逻辑）
     * false: 拥有任意一个角色即可（OR逻辑，默认）
     */
    boolean requireAll() default false;
}

