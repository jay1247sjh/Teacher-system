package com.txq.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)       // 注解只能在类上
@Retention(RetentionPolicy.RUNTIME)         // 运行时依然存在
@RequestMapping
@RestController
public @interface ApiRequestMapping {

    // 把传入的值赋值给RequestMapping的value属性
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String value() default "";
}
