package com.vmsmia.framework.component.rpc.restful.annotation.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Delete注解类，用于标记RESTful API中的DELETE方法.
 * 这个注解适用于方法级别，允许框架在运行时识别并处理标记了此注解的方法。
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:41
 * @since 1.8
 * @Retention(RUNTIME) 表示此注解在运行时可被读取，因此可以在运行时对方法进行注解处理。
 * @Target(METHOD) 表示此注解只能用于标记方法。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
}
