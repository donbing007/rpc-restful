package com.vmsmia.framework.component.rpc.restful.loadbalancer.impl;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.AbstractLoadBalancer;
import java.util.List;

/**
 * 具体实现了轮询负载均衡的类，继承自{@link AbstractLoadBalancer}.
 * <p>
 * 该类实现了基于轮询算法的负载均衡策略.在一组端点中，每次选择操作将按顺序选择下一个端点，
 * 当达到端点列表的末尾后，再次从列表的开始选择端点，这样形成一个循环.通过这种方式，
 * 可以相对平均地将请求或任务分配给每个端点，从而实现负载均衡.
 * </p>
 * <p>
 * 轮询算法简单且有效，适用于端点处理能力相对均匀的场景.在多线程环境下，该类通过内部同步机制
 * 确保线程安全，避免在并发选择端点时产生状态不一致的问题.
 * </p>
 * 使用轮询负载均衡器时，只需要提供一个端点列表，负载均衡器将负责维护当前选择的位置并在每次选择时更新.
 * 这使得轮询负载均衡器在各种分布式系统中广泛应用，尤其是需要简单负载均衡而无需考虑端点间差异的场景.
 *
 * @author bin.dong
 * @version 0.1 2024/4/26 10:59
 * @see AbstractLoadBalancer 轮询负载坐衡器的基类
 * @since 1.8
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    private final Object locker = new Object();
    private int lastSelectIndex;

    public RoundRobinLoadBalancer() {
        super();
        lastSelectIndex = 0;
    }

    @Override
    protected Endpoint doSelect(List<Endpoint> endpoints) {
        int newIndex;
        synchronized (locker) {
            newIndex = (lastSelectIndex + 1) % endpoints.size();
            lastSelectIndex = newIndex;
        }
        return endpoints.get(newIndex);
    }
}
