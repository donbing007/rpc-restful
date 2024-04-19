package com.vmsmia.framework.component.rpc.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 超时配置.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 10:40
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timeout {

    /**
     * 读取的超时时间.单位毫秒.
     */
    long readTimeoutMs() default 10000L;

    /**
     * 连接的超时时间.单位毫秒.
     */
    long connectTimeoutMs() default 5000L;

    /**
     * 写入的超时时间.单位毫秒.
     */
    long writeTimeoutMs() default 5000L;
}
