package com.vmsmia.framework.component.rpc.restful.annotation.parameter;

import com.vmsmia.framework.component.rpc.restful.serializer.Serializer;
import com.vmsmia.framework.component.rpc.restful.serializer.string.json.JsonSerializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 请求体.
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 14:48
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Body {

    /**
     * 请求体的媒体类型.
     */
    String mediaType() default "application/json; charset=utf8";

    /**
     * 媒体类型的编码序列化器.
     */
    Class<? extends Serializer> serializer() default JsonSerializer.class;
}
