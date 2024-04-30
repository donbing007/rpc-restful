package com.vmsmia.framework.component.rpc.restful.annotation.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Head注解类定义.
 * 该注解用于标记方法，无参数和返回值。
 * 主要用于运行时获取注解信息。
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:41
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Head {
}
