package com.vmsmia.framework.component.rpc.restful.discovery;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 基于Kubernetes服务的服务发现组件.
 *
 * <p>
 * 该类提供了一种在Kubernetes集群环境中进行服务发现的机制。它利用Kubernetes API来查询特定服务的信息，并将查询结果缓存以提高性能。
 * </p>
 * 主要工作原理如下：
 * <ul>
 *     <li>通过Kubernetes客户端建立与Kubernetes API的连接，允许查询当前集群中的服务（Service）信息。</li>
 *     <li>利用Caffeine缓存库，对服务的查询结果进行本地缓存处理，减少对Kubernetes API的重复查询，从而提高查询效率和响应速度。</li>
 *     <li>使用Kubernetes Watcher机制实时监听服务（Service）的添加、修改和删除事件，以便及时更新本地缓存，保证服务发现的准确性。</li>
 *     <li>支持在指定命名空间内或所有命名空间内进行服务发现，以满足不同服务部署策略的需求。</li>
 *     <li>提供服务发现接口，允许通过服务名称进行查询，返回可能的服务端点列表，以支持服务间的互相调用。</li>
 * </ul>
 * 通过以上机制，本组件既减少了对Kubernetes API的直接依赖，又确保了服务发现的时效性和准确性，适用于构建在Kubernetes平台上的微服务架构中。
 *
 * @author bin.dong
 * @version 0.1 2024/4/22 17:38
 * @since 1.8
 */
public class KubernetesServiceDiscover implements Discovery {

    private static final String SERVICE_METADATA_NAME = "metadata.name";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final EndpointsValue EMPTY_ENDPOINTS_VALUE =
        new EndpointsValue(Collections.emptyList(), null);
    private final boolean allNamespace;
    private final Cache<String, EndpointsValue> cache;
    private String namespace;

    private KubernetesClient kubernetesClient;
    private boolean privateKubernetesClient;

    private Watch serviceWatch;

    public KubernetesServiceDiscover(boolean allNamespace) {
        this(allNamespace, 1000 * 60 * 60, null);
    }

    public KubernetesServiceDiscover(boolean allNamespace, long effectiveTimeMs) {
        this(allNamespace, effectiveTimeMs, null);
    }

    public KubernetesServiceDiscover(long effectiveTimeMs) {
        this(false, effectiveTimeMs, null);
    }

    /**
     * 构造一个新的服务发现实例.
     *
     * @param allNamespace     true 如果当前命名空间找不到,就在所有命名空间中查找.
     * @param effectiveTimeMs  缓存过期毫秒.
     * @param kubernetesClient Kubernetes客户端. 可为null,内部会以默认配置创建一个.
     */
    public KubernetesServiceDiscover(
        boolean allNamespace, long effectiveTimeMs, KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.allNamespace = allNamespace;

        this.cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(Duration.ofMillis(effectiveTimeMs))
            .build();
    }

    @PostConstruct
    public void init() {

        if (kubernetesClient == null) {
            kubernetesClient = new KubernetesClientBuilder().withConfig(Config.autoConfigure(null)).build();
            privateKubernetesClient = true;
        }
        this.namespace = kubernetesClient.getNamespace();

        this.serviceWatch = this.kubernetesClient.services().inAnyNamespace().watch(new ServiceWatcher(this.cache));
    }

    @PreDestroy
    public void destroy() {
        if (serviceWatch != null) {
            serviceWatch.close();
        }

        if (kubernetesClient != null && this.privateKubernetesClient) {
            // 只处理自己设置的client.
            kubernetesClient.close();
        }
    }

    @Override
    public List<Endpoint> discover(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return Collections.emptyList();
        }

        return find(serviceName).getEndpoints();
    }

    private EndpointsValue find(String serviceName) {
        // 如果没有将触发加载动作,并且保证了同一个key同一时间只有一个加载动作.
        return this.cache.get(serviceName, this::findFromKubernetes);
    }

    private EndpointsValue findFromKubernetes(String serviceName) {
        ServiceList serviceList = kubernetesClient
            .services()
            .inNamespace(namespace)
            .withField(SERVICE_METADATA_NAME, serviceName)
            .list();
        Service svc = selectService(serviceList);
        if (svc == null && allNamespace) {

            serviceList = kubernetesClient
                .services()
                .inAnyNamespace()
                .withField(SERVICE_METADATA_NAME, serviceName)
                .list();
            svc = selectService(serviceList);

        }

        if (svc != null) {
            String namespace = svc.getMetadata().getNamespace();
            return new EndpointsValue(parseEndpoint(serviceName, svc), namespace);
        } else {
            /*
             由于没有找到对应的服务,这时候设定一个空值表示防止缓存穿透.
             这里依赖于watch机制的淘汰,当任何namespace的svc被创建如果名称符合那么这个应该被淘汰.
             */
            return EMPTY_ENDPOINTS_VALUE;
        }
    }

    private List<Endpoint> parseEndpoint(String serviceName, Service svc) {
        Optional<ServicePort> selectPort = svc.getSpec().getPorts().stream()
            .filter(p ->
                p.getName().toLowerCase().contains(SCHEME_HTTP) || p.getName().toLowerCase().contains(SCHEME_HTTPS))
            .findFirst();

        if (selectPort.isPresent()) {
            ServicePort port = selectPort.get();
            boolean tls = port.getName().toLowerCase().contains(SCHEME_HTTPS);

            return Collections.singletonList(new Endpoint(serviceName, port.getPort(), tls, 0.0F));
        } else {
            return Collections.emptyList();
        }
    }

    // 返回值可能为null.
    private Service selectService(ServiceList serviceList) {
        List<Service> services = serviceList.getItems();
        if (services.isEmpty()) {
            return null;
        } else {
            return services.stream()
                .filter(svc -> svc.getSpec().getType().equals("ClusterIP"))
                .findFirst()
                .orElse(null);
        }
    }

    private static class EndpointsValue {
        private final List<Endpoint> endpoints;
        private final String namespace;

        public EndpointsValue(List<Endpoint> endpoints, String namespace) {
            this.endpoints = endpoints;
            this.namespace = namespace;
        }

        public List<Endpoint> getEndpoints() {
            return endpoints;
        }

        public String getNamespace() {
            return namespace;
        }

        public boolean isUnknownNamespace() {
            return namespace == null || namespace.isEmpty();
        }
    }

    private static class ServiceWatcher implements Watcher<Service> {

        private final Cache<String, EndpointsValue> cache;

        public ServiceWatcher(Cache<String, EndpointsValue> cache) {
            this.cache = cache;
        }

        @Override
        public void eventReceived(Action action, Service service) {
            switch (action) {
                case ADDED:
                case MODIFIED:
                case DELETED:
                    /*
                    如果发生服务变化,那么清除缓存触发重新加载.
                    淘汰的判定原则不光名称一致,还要判断所属的namespace是否一致.
                    注意: 如果缓存中存放的是 EMPTY_ENDPOINTS_VALUE 表示之前没有找到对应的服务,任何namespace的服务变化如果名称匹配都会
                    造成缓存中的值被淘汰.
                     */
                    String namespace = service.getMetadata().getNamespace();
                    String serviceName = service.getMetadata().getName();
                    EndpointsValue endpointsValue = cache.getIfPresent(serviceName);
                    if (endpointsValue != null
                        && (endpointsValue.getNamespace().equals(namespace) || endpointsValue.isUnknownNamespace())) {
                        cache.invalidate(serviceName);
                    }
                    break;
                default: {
                    // do nothing.
                }
            }
        }

        @Override
        public void onClose(WatcherException cause) {
            // 什么也不做,保持当前的端点缓存不变.
        }
    }
}
