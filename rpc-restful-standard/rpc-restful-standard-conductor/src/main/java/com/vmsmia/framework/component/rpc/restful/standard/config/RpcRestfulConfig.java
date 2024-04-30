package com.vmsmia.framework.component.rpc.restful.standard.config;

/**
 * @author bin.dong
 * @version 0.1 2024/4/30 19:24
 * @since 1.8
 */
public class RpcRestfulConfig {
    private Integer writeTimeoutMs;
    private Integer readTimeoutMs;
    private Integer connectTimeoutMs;
    private Integer threadPoolSize;
    private Integer maxRequest;
    private DiscoveryConfig discovery;
    private LoadBalancerConfig loadBalancer;

    // Getters and Setters
    public Integer getWriteTimeoutMs() {
        return writeTimeoutMs == null ? 10000 : writeTimeoutMs;
    }

    public void setWriteTimeoutMs(Integer writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }

    public Integer getReadTimeoutMs() {
        return readTimeoutMs == null ? 10000 : readTimeoutMs;
    }

    public void setReadTimeoutMs(Integer readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Integer getConnectTimeoutMs() {
        return connectTimeoutMs == null ? 10000 : connectTimeoutMs;
    }

    public void setConnectTimeoutMs(Integer connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public DiscoveryConfig getDiscovery() {
        return discovery;
    }

    public void setDiscovery(DiscoveryConfig discovery) {
        this.discovery = discovery;
    }

    public LoadBalancerConfig getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerConfig loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize == null ? 10 : threadPoolSize;
    }

    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public Integer getMaxRequest() {
        return maxRequest == null ? 200 : maxRequest;
    }

    public void setMaxRequest(Integer maxRequest) {
        this.maxRequest = maxRequest;
    }
}
