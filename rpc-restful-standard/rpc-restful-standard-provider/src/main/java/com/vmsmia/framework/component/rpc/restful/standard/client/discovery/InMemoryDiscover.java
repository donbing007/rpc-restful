package com.vmsmia.framework.component.rpc.restful.standard.client.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基于内存的服务发现实现,主要用以测试使用.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 17:20
 * @since 1.8
 */
public class InMemoryDiscover implements Discovery {

    private static final InMemoryDiscover INSTANCE = new InMemoryDiscover();
    private final Map<String, Endpoint> endpoints;

    public static InMemoryDiscover getInstance() {
        return INSTANCE;
    }

    private InMemoryDiscover() {
        endpoints = new HashMap<>();
    }

    @Override
    public Optional<Endpoint> discover(String serviceName) {
        return Optional.ofNullable(endpoints.get(serviceName));
    }

    public void register(String serviceName, Endpoint endpoint) {
        endpoints.put(serviceName, endpoint);
    }

    public void reset() {
        this.endpoints.clear();
    }

}
