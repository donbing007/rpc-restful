package com.vmsmia.framework.component.rpc.restful.standard.generation.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.TypeKindVisitor8;

/**
 * @author bin.dong
 * @version 0.1 2024/4/12 10:40
 * @since 1.8
 */
public class AnnotationHelper {

    /**
     * 获取指定元素上的注解信息.
     */
    public static List<AnnotationDefinition> parseAnnotations(Element element) {
        return element.getAnnotationMirrors().stream()
            .map(am -> {
                TypeElement annotationTypeElement = (TypeElement) am.getAnnotationType().asElement();
                AnnotationDefinition definition =
                    new AnnotationDefinition(annotationTypeElement.getQualifiedName().toString());

                am.getElementValues().forEach((key, value) -> {
                    definition.saveValue(key.getSimpleName().toString(), value.getValue());
                });
                return definition;
            }).collect(Collectors.toList());
    }

    /**
     * 获取类信上的所有注解.
     */
    public static List<AnnotationDefinition> parseClassAnnotation(TypeElement typeElement) {
        return parseAnnotations(typeElement);
    }

    /**
     * 获取所有相关的注解,一般是一个方法定义的所有注解.包括方法体和入参上.
     */
    public static List<AnnotationDefinition> parseMethodAnnotation(ExecutableElement methodElement) {
        return parseAnnotations(methodElement);
    }

    /**
     * 方法参数上的所有注解.
     */
    public static List<ParameterAnnotationDefinition> parseMethodParameterAnnotation(
        ExecutableElement methodElement) {
        List<ParameterAnnotationDefinition> annotationDefinitions = new ArrayList<>();

        for (VariableElement parameterElement : methodElement.getParameters()) {
            String parameterName = parameterElement.getSimpleName().toString();
            for (AnnotationMirror annotationMirror : parameterElement.getAnnotationMirrors()) {
                TypeElement annotationTypeElement =
                    (TypeElement) annotationMirror.getAnnotationType().asElement();
                String fqn = annotationTypeElement.getQualifiedName().toString();
                ParameterAnnotationDefinition
                    definition = new ParameterAnnotationDefinition(fqn, parameterName);

                // 处理注解默认值.
                annotationTypeElement.getEnclosedElements()
                    .stream()
                    .filter(e -> e.getKind() == ElementKind.METHOD)
                    .map(e -> (ExecutableElement) e)
                    .forEach(method -> {
                        AnnotationValue defaultValue = method.getDefaultValue();
                        if (defaultValue != null) {
                            definition.saveValue(
                                method.getSimpleName().toString(),
                                defaultValue.accept(new SafeAnnotationValueVisitor(), null));
                        }
                    });

                // 处理非默认值.
                annotationMirror.getElementValues().forEach((methodElem, annotationValue) -> {
                    Object value = annotationValue.accept(new SafeAnnotationValueVisitor(), null);
                    definition.saveValue(methodElem.getSimpleName().toString(), value);
                });
                annotationDefinitions.add(definition);
            }

        }
        return annotationDefinitions;
    }

    /*
    注解值读取器.
    所有非原生类型都是以FQN全限定名被返回.即是一个表示Class的字符串.
     */
    static class SafeAnnotationValueVisitor extends SimpleAnnotationValueVisitor8<Object, Void> {

        @Override
        public Object visitType(TypeMirror t, Void unused) {
            return t.accept(new TypeKindVisitor8<Object, Void>() {
                @Override
                public Object visitDeclared(DeclaredType t, Void unused1) {
                    return t.accept(new SimpleTypeVisitor8<String, Void>() {
                        @Override
                        public String visitDeclared(DeclaredType t, Void unused2) {
                            TypeElement typeElement = (TypeElement) t.asElement();
                            return typeElement.getQualifiedName().toString();
                        }
                    }, null);
                }
            }, null);
        }

        @Override
        public Object visitString(String s, Void unused) {
            return s;
        }

        @Override
        public Object visitLong(long i, Void unused) {
            return i;
        }

        @Override
        public Object visitInt(int i, Void unused) {
            return i;
        }
    }
}
