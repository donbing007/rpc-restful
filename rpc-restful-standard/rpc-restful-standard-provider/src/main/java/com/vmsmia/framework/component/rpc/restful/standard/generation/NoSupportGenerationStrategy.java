package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.MethodGenerationHelper;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author bin.dong
 * @version 0.1 2024/4/9 15:51
 * @since 1.8
 */
public class NoSupportGenerationStrategy implements MethodGenerationStrategy {

    private static final NoSupportGenerationStrategy METHOD_IMPL_STRATEGY = new NoSupportGenerationStrategy();

    public static MethodGenerationStrategy getInstance() {
        return METHOD_IMPL_STRATEGY;
    }

    @Override
    public MethodSpec generate(TypeElement typeElement, ExecutableElement methodElement, ProcessingEnvironment processingEnv) {
        CodeBlock codeBlock = CodeBlock.builder()
            .addStatement("throw new $T($S)", UnsupportedOperationException.class,
                "No valid Method annotation was found.")
            .build();

        return MethodGenerationHelper.generateInterfaceMethodImpl(methodElement, codeBlock);
    }

    private NoSupportGenerationStrategy() {
    }
}