package com.vmsmia.framework.component.rpc.restful.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注接口为 restful client的封装.
 *
 * @author bin.dong
 * @version 0.1 2024/4/8 16:19
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestfulClient {

    /**
     * <p>
     * discover://{服务名称}:{端口}<br>
     * 此种方式表示这是一个需要服务发现的名称, 端口如果不指定将由服务发现进行动态发现.<br>
     * 注意如果显式的指定了端口,那么其会替代服务发现得到的端口.<br>
     * discover://test-service:9230   但是服务发现得到的端口是8080,这时候也会强制使用9230端口.
     * </p>
     * <p>
     * http://{host}:{port:80}/{path}<br>
     * 此种方式表示这是一个固定的地址,不需要服务发现的地址.
     * </p>
     */
    String value();
}
