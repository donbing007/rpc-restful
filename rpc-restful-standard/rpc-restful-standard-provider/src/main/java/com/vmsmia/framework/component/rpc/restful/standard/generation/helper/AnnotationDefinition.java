package com.vmsmia.framework.component.rpc.restful.standard.generation.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author bin.dong
 * @version 0.1 2024/4/10 15:53
 * @since 1.8
 */
public class AnnotationDefinition {
    private final String fqn;
    private final Map<String, Object> values;

    public AnnotationDefinition(String fqn) {
        this.fqn = fqn;
        this.values = new HashMap<>();
    }

    public String getFqn() {
        return fqn;
    }

    public AnnotationDefinition saveValue(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    public Optional<Object> getValue(String key) {
        return Optional.ofNullable(this.values.get(key));
    }

}
