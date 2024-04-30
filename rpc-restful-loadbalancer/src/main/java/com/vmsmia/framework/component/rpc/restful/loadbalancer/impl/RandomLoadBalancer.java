package com.vmsmia.framework.component.rpc.restful.loadbalancer.impl;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.AbstractLoadBalancer;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 通过随机算法实现的负载均衡器，继承自{@link AbstractLoadBalancer}.
 * <p>
 * 此类提供了一种随机选择端点的负载均衡策略.在一组给定的端点中，每次选择操作都会随机选择一个端点，
 * 不考虑端点之前的选择历史或端点的负载情况.这种方法简单且在端点处理能力大致相等时能够提供良好的负载分散效果，
 * 尤其是在无状态的服务场景下.
 * </p>
 * <p>
 * 随机选择算法适用于端点数量相对固定且每个端点的处理能力相近的场景.
 * 它的实现依赖于{@link ThreadLocalRandom}，这是一个高效的随机数生成器，
 * 比{@link java.util.Random}在并发环境中具有更好的性能，且能够减少线程间的争用.
 * </p>
 * 使用此类时，不需要额外的同步或状态维护机制，因为每次选择都是完全独立且基于当前所有可用端点进行的.
 * 这也意味着，在动态变化的端点列表中，每个端点被选中的概率都是相等的，只受端点总数的影响.
 *
 * @author bin.dong
 * @version 0.1 2024/4/26 10:31
 * @see AbstractLoadBalancer 此类的基类，提供了负载均衡的基本框架
 * @see Endpoint 表示可供选择的端点
 * @see ThreadLocalRandom 用于生成选择索引的随机数生成器
 * @since 1.8
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    public RandomLoadBalancer() {
        super();
    }

    @Override
    protected Endpoint doSelect(List<Endpoint> endpoints) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // [0, endpoints.size())
        int index = random.nextInt(endpoints.size());
        return endpoints.get(index);
    }
}
