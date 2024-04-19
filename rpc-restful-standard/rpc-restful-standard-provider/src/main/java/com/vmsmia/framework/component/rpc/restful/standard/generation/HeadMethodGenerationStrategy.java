package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.Request;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 17:46
 * @since 1.8
 */
public class HeadMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // List<Map.Entry<String, String>> {callResultVariableName} = {httpClientVariableName}.head();

        return CodeBlock.builder()
            .addStatement(
                "$T<$T<$T, $T>> $L = $L.head()",
                List.class,
                Map.Entry.class,
                String.class,
                String.class,
                callResultVariableName,
                httpClientVariableName)
            .build();
    }
}
