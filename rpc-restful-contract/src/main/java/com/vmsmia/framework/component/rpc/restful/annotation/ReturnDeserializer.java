package com.vmsmia.framework.component.rpc.restful.annotation;

import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应解码器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 20:28
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReturnDeserializer {

    /**
     * 解码器类型.<br>
     * 所有解码器的实现都预期有一个 getInstance() 静态方法.
     */
    Class<? extends BytesDeserializer> value();
}
