package com.vmsmia.framework.component.rpc.restful.loadbalancer.impl;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.AbstractLoadBalancer;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现了抽象负载均衡器的最少请求负载均衡策略类.
 *
 * <p>
 * 该负载均衡器通过维护一个并发映射，记录每个端点的请求计数和最后更新时间，
 * 以实现动态地选择当前请求数最少的端点.此方法旨在平衡系统负载，通过分散请求至不同的端点来减少任一端点的压力.
 * </p>
 *
 * <p>
 * 当收到新的请求时，本负载均衡策略会遍历所有可用端点，
 * 选出当前活跃请求数最少的端点作为请求的目标.如果存在多个端点的请求数同时最少，
 * 则选择最后更新时间最早的端点，以确保公平性并考虑到可能的时序问题.
 * </p>
 *
 * <p>
 * 对请求的释放也进行了处理，确保每次请求完成后更新对应端点的活跃请求数，保证了负载均衡决策的准确性.
 * </p>
 *
 * <p>
 * 类中包含的{@code RequestCount}的静态内部类用于跟踪每个端点的请求计数和最后更新时间.
 * 该内部类实现了{@code Comparable}接口，以便能够根据请求计数和最后更新时间对端点进行排序，
 * 进而支持负载均衡策略的实施.
 * </p>
 *
 * <p>
 * 注意，尽管尽力确保线程安全和更新操作的原子性，但在高并发情况下，
 * 计数器的准确性可能会受到一定程度的影响，这是并发更新操作中无法完全避免的问题.<br>
 * <em>必须保证命名用时遵守try-with-resources,否则会造成内存泄漏.</em>
 * </p>
 *
 * @author bin.dong
 * @version 0.1 2024/4/28 11:56
 * @since 1.8
 */
public class LeastRequestLoadBalancer extends AbstractLoadBalancer {

    private final ConcurrentMap<Endpoint, RequestCount> requestCountMap;
    private final Comparator<Map.Entry<Endpoint, RequestCount>> comparator;

    public LeastRequestLoadBalancer() {
        requestCountMap = new ConcurrentHashMap<>();
        this.comparator = new MapEntryComparator();
    }

    @Override
    public void release(Endpoint endpoint) {
        requestCountMap.compute(endpoint, (key, old) -> {
            if (old != null) {
                if (old.decrement() <= 0) {
                    return null;
                } else {
                    return old;
                }
            } else {
                return null;
            }
        });
    }

    /**
     * 活跃的端点.
     */
    public int activeEndpoint() {
        return requestCountMap.size();
    }

    /**
     * 获得取指定端点的请求计数.
     *
     * @param endpoint 目标端点.
     * @return 计数, 如果没有将小于0.
     */
    public long getEndpointCount(Endpoint endpoint) {
        RequestCount count = requestCountMap.get(endpoint);
        if (count != null) {
            return count.getCount();
        } else {
            return -1;
        }
    }

    protected Endpoint doSelect(List<Endpoint> endpoints) {
        // 这里可以直接返回的原因是如果计算不存在会创建一个新的,且计数值为0.
        Map.Entry<Endpoint, RequestCount> selected = endpoints.stream()
            .map(e -> {
                RequestCount count = requestCountMap.get(e);
                if (count == null) {
                    count = new RequestCount(0L);
                    return new AbstractMap.SimpleEntry<>(e, count);
                }
                return new AbstractMap.SimpleEntry<>(e, count);
            }).min(comparator).get();

        // 检查选择结果是否在requestCountMap中.
        requestCountMap.compute(selected.getKey(), (key, old) -> {
            if (old == null) {
                RequestCount newCount = selected.getValue();
                newCount.increment();
                return newCount;
            } else {
                old.increment();
                return old;
            }
        });
        return selected.getKey();
    }

    private static class MapEntryComparator implements Comparator<Map.Entry<Endpoint, RequestCount>> {
        @Override
        public int compare(Map.Entry<Endpoint, RequestCount> o1, Map.Entry<Endpoint, RequestCount> o2) {
            RequestCount c1 = o1.getValue();
            RequestCount c2 = o2.getValue();
            return c1.compareTo(c2);
        }
    }

    private static class RequestCount implements Comparable<RequestCount> {
        private long count;
        private long lastUpdateTime;

        public RequestCount(long initCount) {
            this.count = initCount;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public long getCount() {
            return this.count;
        }

        public long getLastUpdateTime() {
            return this.lastUpdateTime;
        }

        public synchronized long increment() {
            this.count++;
            this.lastUpdateTime = System.currentTimeMillis();
            return this.count;
        }

        public synchronized long decrement() {
            this.count--;
            this.lastUpdateTime = System.currentTimeMillis();
            return this.count;
        }

        @Override
        public int compareTo(RequestCount o) {
            int countComparison = Long.compare(this.count, o.count);
            if (countComparison != 0) {
                return countComparison;
            }
            return Long.compare(o.lastUpdateTime, this.lastUpdateTime);
        }
    }
}
