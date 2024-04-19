package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;
import java.io.IOException;
import okhttp3.Request;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 17:46
 * @since 1.8
 */
public class PostMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // {returnTypeFqn} {callResultVariableName} = {httpClientVariableName}.post(Class.forName("{returnTypeFqn}"));
        return CodeBlock.builder()
            .addStatement("$L = ($L) $L.post(Class.forName($S))",
                callResultVariableName, returnTypeFqn, httpClientVariableName, returnTypeFqn)
            .build();
    }
}
