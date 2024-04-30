package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;

/**
 * 请求method是Stream的生成策略.
 *
 * @author bin.dong
 * @version 0.1 2024/4/24 9:46
 * @since 1.8
 */
public class StreamMethodGenerationStrategy extends AbstractMethodGenerationStrategy {
    
    @Override
    protected CodeBlock finishedCall(String httpClientVariableName,
                                     String callResultVariableName,
                                     String unused) {
        return CodeBlock.builder()
            .addStatement("$L.stream($L)", httpClientVariableName, callResultVariableName)
            .build();
    }
}
