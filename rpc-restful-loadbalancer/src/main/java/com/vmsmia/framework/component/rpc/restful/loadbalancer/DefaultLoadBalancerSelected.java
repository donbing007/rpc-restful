package com.vmsmia.framework.component.rpc.restful.loadbalancer;


import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;

/**
 * @author bin.dong
 * @version 0.1 2024/4/28 15:50
 * @since 1.8
 */
public class DefaultLoadBalancerSelected implements LoadBalancerSelected {

    private final LoadBalancer loadBalancer;
    private final Endpoint endpoint;

    public DefaultLoadBalancerSelected(LoadBalancer loadBalancer, Endpoint endpoint) {
        this.loadBalancer = loadBalancer;
        this.endpoint = endpoint;
    }

    @Override
    public Endpoint endpoint() {
        return this.endpoint;
    }

    @Override
    public void close() throws Exception {
        loadBalancer.release(endpoint);
    }
}
