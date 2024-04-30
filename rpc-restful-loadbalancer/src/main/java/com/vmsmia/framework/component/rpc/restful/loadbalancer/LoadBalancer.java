package com.vmsmia.framework.component.rpc.restful.loadbalancer;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import java.util.List;
import java.util.Optional;

/**
 * 定义负载均衡器接口，规定了负载均衡的使用方式和行为.在负载均衡场景中，实现此接口的类需要提供端点的选择和释放机制.
 * 这可以支持多种负载均衡算法，例如轮询、最少连接数等，以便在分布式系统中有效地分配请求或任务至不同的服务实例.
 *
 * <p>
 * 负载均衡的选择过程是根据实现类定义的算法来从一组端点中选出最适合当前请求的端点，而释放动作则通常用于更新负载均衡器内部状态，
 * 例如减少端点的活跃请求计数等.这对于那些需要根据实时负载情况进行动态调整的算法尤为重要.
 * </p>
 * 使用示例：
 * <pre>
 * LoadBalancer loadBalancer =
 *              LoadBalancerFactory.getLoadBalancer("ROUND_ROBIN");
 * Optional&lt;LoadBalancerSelected&gt; selected = loadBalancer.select(endpoints);
 * if (selected.isPresent()) {
 *     try (LoadBalancerSelected selectedResource = selected.get()) {
 *         Endpoint endpoint = selectedResource.endpoint();
 *         // 在此处使用选定的端点进行操作
 *     } // 在此自动释放选择的端点.
 * } else {
 *     // 处理无可用端点的情况
 * }
 * </pre>
 *
 * @author bin.dong 2024/4/26 10:03
 * @version 0.1
 * @apiNote 此接口的具体实现需要确保线程安全，特别是在高并发场景下对端点的选择和释放动作.
 * @implSpec 实现此接口时, 必须提供一个无参的构造函数.
 * @implSpec 实现此接口时，应适当考虑到在分布式环境下实现负载均衡逻辑的复杂性和对性能的要求.
 * @implNote 实现类可以选择缓存端点信息，但需要注意缓存更新和过期策略，避免因信息陈旧导致负载分配不均.
 * @see LoadBalancerFactory 用于创建LoadBalancer实例的工厂类
 * @see LoadBalancerSelected 表示通过负载均衡器选定的资源及其释放机制的接口
 * @see Endpoint 表示可供选择的端点的类
 * @since 1.8
 */
public interface LoadBalancer {

    /**
     * 选择一个端点以用于后续的请求处理.端点的选择是根据实现类定义的负载均衡算法进行的，
     * 并且该方法返回的是一个可能包含所选端点的{@code Optional}实例.
     *
     * <p>
     * 如果当前没有任何端点可用，或者根据算法无法选择端点，则返回的{@code Optional}将为空.
     * </p>
     *
     * @param endpoints 当前可用的端点集合，不应为 {@code null}.
     * @return 一个{@code Optional}对象，可能包含符合负载均衡算法选择条件的端点.
     */
    Optional<LoadBalancerSelected> select(List<Endpoint> endpoints);

    /**
     * 释放之前通过{@code select}方法选定的端点.对端点的释放通常是指通知负载均衡器当前请求已处理完成，
     * 负载均衡器可以根据自身算法更新内部状态，如减少端点的活跃请求计数等.
     *
     * @param endpoint 需要释放的端点，此端点之前应由该负载均衡器选定.
     */
    void release(Endpoint endpoint);
}

