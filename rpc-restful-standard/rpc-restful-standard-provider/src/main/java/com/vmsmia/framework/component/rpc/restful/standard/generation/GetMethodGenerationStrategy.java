package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;

/**
 * @author bin.dong
 * @version 0.1 2024/4/12 11:14
 * @since 1.8
 */
public class GetMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // {returnTypeFqn} {callResultVariableName} = {httpClientVariableName}.get(Class.forName("{returnTypeFqn}"));
        return CodeBlock.builder()
            .addStatement("$L = ($L) $L.get(Class.forName($S))",
                callResultVariableName, returnTypeFqn, httpClientVariableName, returnTypeFqn)
            .build();
    }
}
