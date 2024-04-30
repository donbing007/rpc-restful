package com.vmsmia.framework.component.rpc.restful.standard.config;

/**
 * @author bin.dong
 * @version 0.1 2024/4/30 19:22
 * @since 1.8
 */
public class KubernetesConfig {
    private Boolean allNamespace;
    private Integer effectiveTimeMs;

    public Boolean getAllNamespace() {
        return allNamespace == null ? Boolean.FALSE : allNamespace;
    }

    public void setAllNamespace(Boolean allNamespace) {
        this.allNamespace = allNamespace;
    }

    public Integer getEffectiveTimeMs() {
        return effectiveTimeMs == null ? 1000 * 60 * 60 : effectiveTimeMs;
    }

    public void setEffectiveTimeMs(Integer effectiveTimeMs) {
        this.effectiveTimeMs = effectiveTimeMs;
    }
}
