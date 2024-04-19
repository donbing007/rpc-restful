package com.vmsmia.framework.component.rpc.restful.standard.generation.helper;

/**
 * @author bin.dong
 * @version 0.1 2024/4/10 16:13
 * @since 1.8
 */
public class ParameterAnnotationDefinition extends AnnotationDefinition {

    // 定义的目标入参名称.
    private final String parameterName;

    public ParameterAnnotationDefinition(String fqn, String parameterName) {
        super(fqn);
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }
}
