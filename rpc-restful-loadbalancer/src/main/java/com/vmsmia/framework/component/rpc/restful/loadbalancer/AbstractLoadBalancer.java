package com.vmsmia.framework.component.rpc.restful.loadbalancer;

import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import java.util.List;
import java.util.Optional;

/**
 * 抽象负载均衡器类，为负载均衡器的实现提供了基础框架.
 * <p>
 * 本类实现了{@link LoadBalancer}接口的{@code select}方法，
 * 提供了端点选择的基本逻辑，包括处理端点列表为空、仅有一个端点和多个端点的情况.
 * 实际的端点选择策略留给了具体的子类通过实现{@code doSelect}方法来定义.
 * </p>
 *
 * <p>
 * 当端点列表为空或传入的端点列表为{@code null}时，{@code select}方法将返回一个空的{@code Optional}.
 * 当列表中仅有一个端点时，直接选择该端点，不需进一步的选择逻辑.
 * 当列表中有多个端点时，该方法将调用{@code doSelect}方法，此方法需要由子类具体实现，
 * 根据特定的负载均衡策略来选择一个端点.
 * </p>
 *
 * <p>
 * 对于{@code release}方法，默认实现为空操作，子类可以根据需要对其进行覆盖，
 * 以提供释放选定端点后所需执行的操作，如更新端点的状态或统计信息等.
 * </p>
 * 本类及其子类的实现应确保线程安全，以适用于高并发的环境.不同的负载均衡策略，如轮询、随机、
 * 最少连接数等，可通过继承本类并实现{@code doSelect}方法来具体实现.
 *
 * @author bin.dong
 * @version 0.1 2024/4/28 11:29
 * @see LoadBalancer 负载均衡器接口
 * @see Endpoint 端点的表示，选择的目标对象
 * @see LoadBalancerSelected 负载均衡器选择结果的表示
 * @since 1.8
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    /**
     * 选择一个可用的Endpoint.
     *
     * @param endpoints 可用的Endpoint列表
     * @return 可用的Endpoint
     */
    public Optional<LoadBalancerSelected> select(List<Endpoint> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return Optional.empty();
        } else {
            Endpoint endpoint = doSelect(endpoints);
            if (endpoint == null) {
                return Optional.empty();
            } else {
                return Optional.of(new DefaultLoadBalancerSelected(this, endpoint));
            }
        }
    }

    /**
     * 实际什么都没有做,需要由子类决定释放的动作.这是为了某些算法需要追踪端点状态.
     */
    @Override
    public void release(Endpoint endpoint) {
        // 什么也不做,子类根据需要覆盖.
    }

    /**
     * 由子类实现选择逻辑.
     *
     * @param endpoints 被选择的端点列表.
     * @return 选择的端点.
     */
    protected abstract Endpoint doSelect(List<Endpoint> endpoints);
}
