package com.vmsmia.framework.component.rpc.restful.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于内存的服务发现实现,主要用以测试使用.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 17:20
 * @since 1.8
 */
public class InMemoryDiscover implements Discovery {

    private static final InMemoryDiscover INSTANCE = new InMemoryDiscover();
    private final Map<String, List<Endpoint>> endpoints;

    public static InMemoryDiscover getInstance() {
        return INSTANCE;
    }

    private InMemoryDiscover() {
        endpoints = new HashMap<>();
    }

    @Override
    public List<Endpoint> discover(String serviceName) {
        List<Endpoint> es = endpoints.get(serviceName);
        if (es == null) {
            return Collections.emptyList();
        } else {
            Collections.sort(es);
            return es;
        }
    }

    public void register(String serviceName, Endpoint endpoint) {
        List<Endpoint> es = this.endpoints.computeIfAbsent(serviceName, k -> new ArrayList<>());
        es.add(endpoint);
    }

    public void reset() {
        this.endpoints.clear();
    }

}
