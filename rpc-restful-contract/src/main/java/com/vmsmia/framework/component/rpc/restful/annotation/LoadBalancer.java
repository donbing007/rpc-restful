package com.vmsmia.framework.component.rpc.restful.annotation;

import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancers;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定负载均衡算法.
 *
 * @author bin.dong
 * @version 0.1 2024/4/28 11:53
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadBalancer {

    /**
     * 负载均衡算法.
     */
    String value() default LoadBalancers.ROUND_ROBIN;
}
