package com.vmsmia.framework.component.rpc.restful.loadbalancer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vmsmia.framework.component.rpc.restful.common.RandomUtils;
import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancerSelected;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author bin.dong
 * @version 0.1 2024/4/30 10:16
 * @since 1.8
 */
public class LeastRequestLoadBalancerTest {
    private static final String THREAD_SIZE_TAG_NAME = "thread";
    private ExecutorService worker;
    private LeastRequestLoadBalancer loadBalancer;

    @BeforeEach
    void setUp(TestInfo info) {
        loadBalancer = new LeastRequestLoadBalancer();
        for (String tag : info.getTags()) {
            if (tag.startsWith(THREAD_SIZE_TAG_NAME)) {
                int thread = Integer.parseInt(tag.substring((THREAD_SIZE_TAG_NAME + "=").length()));
                worker = Executors.newFixedThreadPool(thread);
            }
        }
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        loadBalancer = null;
        if (worker != null) {
            worker.shutdown();
            worker.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    /**
     * 测试当LeastRequestLoadBalancer为空时,所有端点都是第一次进入.<br>
     * 预期会返回首先碰到的端点.
     */
    @Test
    public void testEmpty() throws Exception {
        List<Endpoint> endpoints = buildEndpoints(10);
        Optional<LoadBalancerSelected> selected = loadBalancer.select(endpoints);
        assertTrue(selected.isPresent());

        LoadBalancerSelected s = selected.get();
        Endpoint selectedEndpoint = s.endpoint();
        assertTrue(endpoints.contains(selectedEndpoint));
        assertEquals(1, loadBalancer.activeEndpoint());
        assertEquals(1, loadBalancer.getEndpointCount(selectedEndpoint));
        loadBalancer.release(selectedEndpoint);
        assertEquals(0, loadBalancer.activeEndpoint());
    }

    /**
     * 会有5个线程,分别以相同的Endpoint列表来请求.
     */
    @Test
    @Tag(THREAD_SIZE_TAG_NAME + "=5")
    public void testEmptyConcurrentlySame() throws Exception {
        List<Endpoint> endpoints = buildEndpoints(10);

        // 保证不会有任务阻塞在没有线程可分配.
        int size = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch overLatch = new CountDownLatch(size);
        ConcurrentMap<Endpoint, Long> selectedResult = new ConcurrentHashMap<>();
        for (int i = 0; i < size; i++) {
            worker.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                /*
                 有意不遵守正式的使用规范使用try-with-resources, 这样可以在之后进行状态检查.
                 */
                Optional<LoadBalancerSelected> selected = loadBalancer.select(endpoints);
                selected.ifPresent(
                    loadBalancerSelected -> selectedResult.compute(selected.get().endpoint(), (key, old) -> {
                        if (old == null) {
                            return 1L;
                        } else {
                            return old + 1;
                        }
                    })
                );

                overLatch.countDown();
            });
        }
        startLatch.countDown();
        overLatch.await();

        assertEquals(5, selectedResult.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(selectedResult.size(), loadBalancer.activeEndpoint());

        Collection<Endpoint> selected = selectedResult.keySet();
        for (Endpoint endpoint : selected) {
            assertEquals(selectedResult.get(endpoint), loadBalancer.getEndpointCount(endpoint));
            // 释放次数必须和调用成对.
            for (long i = 0; i < selectedResult.get(endpoint); i++) {
                loadBalancer.release(endpoint);
            }
        }

        assertEquals(0, loadBalancer.activeEndpoint());
    }

    /**
     * 测试第二次时发生了端点变化,多了一个新的端点.
     * 预期第二次时应该使用新的端点,因为连接数最小.
     */
    @Test
    public void testSelectWithOneThenAddNew() throws Exception {
        List<Endpoint> endpoints = buildEndpoints(1);
        // 故意不回收,模似端点正在被使用的情况.
        Optional<LoadBalancerSelected> selected = loadBalancer.select(endpoints);
        assertTrue(selected.isPresent());
        assertEquals(1, loadBalancer.activeEndpoint());
        Endpoint selectedEndpoint = selected.get().endpoint();
        assertEquals(1, loadBalancer.getEndpointCount(selectedEndpoint));

        List<Endpoint> newEndpoints = new ArrayList<>(buildEndpoints(1));
        Endpoint newEndpoint = newEndpoints.get(0);
        newEndpoints.add(selectedEndpoint);
        Collections.sort(newEndpoints);
        selected = loadBalancer.select(newEndpoints);
        assertTrue(selected.isPresent());
        assertEquals(2, loadBalancer.activeEndpoint());
        try (LoadBalancerSelected s = selected.get()) {
            assertEquals(newEndpoint, s.endpoint());
        }

        loadBalancer.release(selectedEndpoint);
        assertEquals(0, loadBalancer.activeEndpoint());
    }

    private List<Endpoint> buildEndpoints(int size) {
        List<Endpoint> endpoints = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            endpoints.add(buildEndpoint());
        }
        Collections.sort(endpoints);
        return endpoints;
    }

    private Endpoint buildEndpoint() {
        String host = RandomUtils.generateRandomString(3, 5);
        int port = RandomUtils.generateRandomInt(80, 65535);
        boolean tls = RandomUtils.generateRandomBoolean();
        float weight = 0.0F;
        return new Endpoint(host, port, tls, weight);
    }

}