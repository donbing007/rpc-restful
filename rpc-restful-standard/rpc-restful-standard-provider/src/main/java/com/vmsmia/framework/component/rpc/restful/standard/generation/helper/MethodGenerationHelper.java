package com.vmsmia.framework.component.rpc.restful.standard.generation.helper;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

/**
 * 用于生成接口方法实现的辅助类.
 *
 * @author bin.dong
 * @version 0.1 2024/4/9 19:32
 * @since 1.8
 */
public class MethodGenerationHelper {

    private MethodGenerationHelper() {
    }

    /**
     * 生成接口方法的实现.
     *
     * @param methodElement 表示方法的可执行元素。
     * @param codeBlock     方法体的代码块。
     * @return 构建好的方法规范（MethodSpec）对象，代表了接口方法的实现。
     */
    public static MethodSpec generateInterfaceMethodImpl(ExecutableElement methodElement, CodeBlock codeBlock) {
        return generateInterfaceMethodImpl(
            methodElement,
            codeBlock,
            Collections.singletonList(
                AnnotationSpec.builder(Override.class).build()
            ));
    }

    /**
     * 生成接口方法的实现，允许指定注解集合.
     *
     * @param methodElement   表示方法的可执行元素。
     * @param codeBlock       方法体的代码块。
     * @param annotationSpecs 方法上要添加的注解集合。
     * @return 构建好的方法规范（MethodSpec）对象，代表了接口方法的实现。
     */
    public static MethodSpec generateInterfaceMethodImpl(
        ExecutableElement methodElement, CodeBlock codeBlock, List<AnnotationSpec> annotationSpecs) {

        String methodName = methodElement.getSimpleName().toString();
        TypeName returnType = TypeName.get(methodElement.getReturnType());
        List<ParameterSpec> parameterSpecs = methodElement.getParameters()
            .stream()
            .map(p ->
                ParameterSpec
                    .builder(TypeName.get(p.asType()), p.getSimpleName().toString())
                    .build()
            ).collect(Collectors.toList());
        List<TypeName> exceptions = methodElement.getThrownTypes()
            .stream()
            .map(TypeName::get)
            .collect(Collectors.toList());

        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addParameters(parameterSpecs)
            .addExceptions(exceptions)
            .returns(returnType)
            .addAnnotations(annotationSpecs == null ? Collections.emptyList() : annotationSpecs)
            .addCode(codeBlock)
            .build();
    }
}
