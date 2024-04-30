package com.vmsmia.framework.component.rpc.restful.discovery;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author bin.dong
 * @version 0.1 2024/4/22 17:38
 * @since 1.8
 */
public class KubernetesServiceInClusterDiscover implements Discovery {

    private static final String SERVICE_METADATA_NAME = "metadata.name";
    private final boolean allNamespace;
    private String namespace;
    private KubernetesClient kubernetesClient;

    public KubernetesServiceInClusterDiscover(boolean allNamespace) {
        this.allNamespace = allNamespace;
    }

    @PostConstruct
    public void init() {
        kubernetesClient = new KubernetesClientBuilder().withConfig(Config.autoConfigure(null)).build();
        this.namespace = kubernetesClient.getNamespace();
    }

    @PreDestroy
    public void destropy() {
        if (kubernetesClient != null) {
            kubernetesClient.close();
        }
    }

    @Override
    public List<Endpoint> discover(String serviceName) {
        ServiceList serviceList = kubernetesClient
            .services()
            .inNamespace(namespace)
            .withField(SERVICE_METADATA_NAME, serviceName)
            .list();
        Service svc = getService(serviceList);
        if (svc == null && allNamespace) {

            serviceList = kubernetesClient
                .services()
                .inAnyNamespace()
                .withField(SERVICE_METADATA_NAME, serviceName)
                .list();
            svc = getService(serviceList);

        }

        if (svc != null) {
            return parseEndpoint(serviceName, svc);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Endpoint> parseEndpoint(String serviceName, Service svc) {
        int port = svc.getSpec().getPorts().get(0).getPort();
        return Collections.singletonList(new Endpoint(serviceName, port, false, 0.0F));
    }

    private Service getService(ServiceList serviceList) {
        List<Service> svcs = serviceList.getItems();
        if (svcs.isEmpty()) {
            return null;
        } else {
            return svcs.stream()
                .filter(svc -> svc.getSpec().getType().equals("ClusterIP")).findFirst().orElse(null);
        }
    }
}
