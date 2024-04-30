package com.vmsmia.framework.component.rpc.restful.loadbalancer;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;

/**
 * 表示一次负载均衡选择结果.
 *
 * @author bin.dong
 * @version 0.1 2024/4/28 15:46
 * @since 1.8
 */
public interface LoadBalancerSelected extends AutoCloseable {

    /**
     * 选择结果中的端点信息.
     */
    Endpoint endpoint();

}
