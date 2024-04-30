package com.vmsmia.framework.component.rpc.restful.standard.config;

/**
 * @author bin.dong
 * @version 0.1 2024/4/30 19:23
 * @since 1.8
 */
public class DiscoveryConfig {

    public static final String KUBERNETES_PROVIDER = "kubernetes";
    public static final String STATIC_PROVIDER = "static";

    private String provider;
    private KubernetesConfig kubernetes;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public KubernetesConfig getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(KubernetesConfig kubernetes) {
        this.kubernetes = kubernetes;
    }
}
