package com.vmsmia.framework.component.rpc.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多个头信息组合.
 *
 * @author bin.dong
 * @version 0.1 2024/4/23 13:46
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestHeads {

    /**
     * 头信息组合.
     */
    RequestHead[] value();
}
