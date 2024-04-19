package com.vmsmia.framework.component.rpc.restful.annotation.parameter;

import com.vmsmia.framework.component.rpc.restful.serializer.string.PlainStringSerializer;
import com.vmsmia.framework.component.rpc.restful.serializer.StringSerializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bin.dong
 * @version 0.1 2024/4/9 14:49
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryParam {
    String value() default "";

    Class<? extends StringSerializer> serializer() default PlainStringSerializer.class;
}
