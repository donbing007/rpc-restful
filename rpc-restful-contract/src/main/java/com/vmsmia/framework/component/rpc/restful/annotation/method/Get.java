package com.vmsmia.framework.component.rpc.restful.annotation.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Get注解类，用于方法上。
 * 这是一个自定义的注解，可以在运行时获取到。
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:40
 * @Retention 表示该注解的生命周期为运行时，即在运行时可以通过反射获取到这个注解
 * @Target 表示该注解可以用于方法上
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Get {
}
