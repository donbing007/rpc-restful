package com.vmsmia.framework.component.rpc.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示请求的URI,即不包含协议,域名和端口的.一定是以"/"开头的字符串.
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:42
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Path {

    /**
     * 以"/"开头的请求URI字符串.
     */
    String value();
}
