package com.vmsmia.framework.component.rpc.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求时附带的头信息.
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:44
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(RequestHeads.class)
public @interface RequestHead {

    /**
     * 请求头信息key.
     */
    String key();

    /**
     * 请求头信息值.
     */
    String val();
}
