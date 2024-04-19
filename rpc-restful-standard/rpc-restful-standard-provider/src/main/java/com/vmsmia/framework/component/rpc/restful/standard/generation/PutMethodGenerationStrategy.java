package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 17:47
 * @since 1.8
 */
public class PutMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // {returnTypeFqn} {callResultVariableName} = {httpClientVariableName}.put(Class.forName("{returnTypeFqn}"));
        return CodeBlock.builder()
            .addStatement("$L = ($L) $L.put(Class.forName($S))",
                callResultVariableName, returnTypeFqn, httpClientVariableName, returnTypeFqn)
            .build();
    }
}
