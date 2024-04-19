package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.MethodSpec;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author bin.dong
 * @version 0.1 2024/4/9 15:09
 * @since 1.8
 */
public interface MethodGenerationStrategy {

    MethodSpec generate(TypeElement classElement, ExecutableElement methodElement, ProcessingEnvironment processingEnv);
}
