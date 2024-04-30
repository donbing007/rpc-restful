package com.vmsmia.framework.component.rpc.restful.loadbalancer;

/**
 * 负载均衡帮助工具.
 *
 * @author bin.dong
 * @version 0.1 2024/4/28 16:14
 * @since 1.8
 */
public class LoadBalancers {

    /**
     * 随机算法.
     */
    public static final String RANDOM = "RANDOM";

    /**
     * 轮询算法.
     */
    public static final String ROUND_ROBIN = "ROUND_ROBIN";

    /**
     * 最少请求算法.
     */
    public static final String LEAST_REQUEST = "LEAST_REQUEST";
}
