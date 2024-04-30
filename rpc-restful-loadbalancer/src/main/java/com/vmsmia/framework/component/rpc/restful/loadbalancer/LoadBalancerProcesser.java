package com.vmsmia.framework.component.rpc.restful.loadbalancer;

import com.vmsmia.framework.component.rpc.restful.common.exception.RestfulException;
import com.vmsmia.framework.component.rpc.restful.discovery.Discovery;
import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 负载均衡方便工具.
 *
 * @author bin.dong
 * @version 0.1 2024/4/29 14:38
 * @since 1.8
 */
public class LoadBalancerProcesser {

    /**
     * 执和方便工具,基于负载均衡和服务发现.
     *
     * @param discovery    服务发现.
     * @param loadBalancer 负载均衡.
     * @param serviceName  服务名称.
     * @param code         执行的代码.
     * @param <T>          执行结果类型.
     * @return 执行结果.
     * @throws RestfulException 可能的异常.
     */
    public static <T> T processWithReturn(
        Discovery discovery,
        LoadBalancer loadBalancer,
        String serviceName,
        Function<Endpoint, T> code) throws RestfulException {

        List<Endpoint> endpoints = discovery.discover(serviceName);
        Optional<LoadBalancerSelected> selected = loadBalancer.select(endpoints);
        if (selected.isPresent()) {
            try (LoadBalancerSelected loadBalancerSelected = selected.get()) {
                Endpoint endpoint = loadBalancerSelected.endpoint();
                return code.apply(endpoint);
            } catch (Exception ex) {
                throw new RestfulException(ex.getMessage(), ex);
            }
        } else {
            throw new RestfulException(String.format("Can not discover %s.", serviceName));
        }
    }

    /**
     * 执和方便工具,基于负载均衡和服务发现.忽略返回.
     *
     * @param discovery    服务发现.
     * @param loadBalancer 负载均衡.
     * @param serviceName  服务名称.
     * @param code         执行的代码.
     * @throws RestfulException 可能的异常.
     */
    public static void processWithNoReturn(
        Discovery discovery,
        LoadBalancer loadBalancer,
        String serviceName,
        Consumer<Endpoint> code) throws RestfulException {

        List<Endpoint> endpoints = discovery.discover(serviceName);
        Optional<LoadBalancerSelected> selected = loadBalancer.select(endpoints);
        if (selected.isPresent()) {
            try (LoadBalancerSelected loadBalancerSelected = selected.get()) {
                Endpoint endpoint = loadBalancerSelected.endpoint();
                code.accept(endpoint);
            } catch (Exception ex) {
                throw new RestfulException(ex.getMessage(), ex);
            }
        } else {
            throw new RestfulException(String.format("Can not discover %s.", serviceName));
        }
    }
}
