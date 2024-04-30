package com.vmsmia.framework.component.rpc.restful.loadbalancer;

import com.vmsmia.framework.component.rpc.restful.loadbalancer.impl.LeastRequestLoadBalancer;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.impl.RandomLoadBalancer;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.impl.RoundRobinLoadBalancer;

/**
 * @author bin.dong
 * @version 0.1 2024/4/28 11:51
 * @since 1.8
 */
public class LoadBalancerFactory {

    /**
     * 如果直接指定了类的FQN,必须以这个开头.
     */
    public static final String CLASS_PREFIX = "class:";

    /**
     * 获取负载均衡器.<br>
     * 注意这里需要保证所有的负载均衡实现都有一个无参的默认构造方法.
     *
     * @param name 负载均衡器名称或者表示负载均衡器类的FQN.
     * @return 负载均衡器实例.
     */
    public static LoadBalancer getLoadBalancer(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("The unrecognized load balancing cannot be identified.");
        }

        switch (name) {
            case LoadBalancers.ROUND_ROBIN:
                return new RoundRobinLoadBalancer();
            case LoadBalancers.LEAST_REQUEST:
                return new LeastRequestLoadBalancer();
            case LoadBalancers.RANDOM:
                return new RandomLoadBalancer();
            default: {
                if (name.startsWith(CLASS_PREFIX)) {
                    // 可能是一个类
                    try {
                        Class<?> loadBalancerClass = Class.forName(name.substring(CLASS_PREFIX.length()));
                        return (LoadBalancer) loadBalancerClass.newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                } else {
                    // 无法识别的字符串
                    throw new RuntimeException(
                        String.format("The unrecognized load balancing (%s) cannot be identified.", name));
                }
            }
        }
    }
}
