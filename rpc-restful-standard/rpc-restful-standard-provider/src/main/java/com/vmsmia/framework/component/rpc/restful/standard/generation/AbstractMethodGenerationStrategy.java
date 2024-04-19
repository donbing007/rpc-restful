package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.vmsmia.framework.component.rpc.restful.RestfulException;
import com.vmsmia.framework.component.rpc.restful.annotation.Header;
import com.vmsmia.framework.component.rpc.restful.annotation.Path;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.annotation.ReturnDeserializer;
import com.vmsmia.framework.component.rpc.restful.annotation.Timeout;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.Body;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.PathVariable;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.QueryParam;
import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.standard.RpcClientProcessor;
import com.vmsmia.framework.component.rpc.restful.standard.client.HttpClient;
import com.vmsmia.framework.component.rpc.restful.standard.client.discovery.DiscoveryHelper;
import com.vmsmia.framework.component.rpc.restful.standard.client.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationDefinition;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationHelper;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.MethodGenerationHelper;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.ParameterAnnotationDefinition;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.UnknownTypeException;
import javax.lang.model.type.WildcardType;

/**
 * @author bin.dong
 * @version 0.1 2024/4/10 15:19
 * @since 1.8
 */
public abstract class AbstractMethodGenerationStrategy implements MethodGenerationStrategy {

    /**
     * 注解默认值的属性名称.
     */
    private static final String ANNOTATION_DEFAULT_FIELD_NAME = "value";
    /**
     * 注解序列化属性的名称.
     */
    private static final String ANNOTATION_SERIALIZER_FIELD_NAME = "serializer";

    private static final String BASE_URL_VARIABLE_NAME = "baseUrl";
    private static final String PATH_TEMPLATE_VARIABLE_NAME = "pathTemplate";
    private static final String PATH_VARIABLE_NAME = "pathVariables";
    private static final String QUERY_PARAM_VARIABLE_NAME = "queryParams";
    private static final String HEADER_VARIABLE_NAME = "headers";
    private static final String BODY_VARIABLE_NAME = "body";
    private static final String BODY_MEDIA_TYPE_VARIABLE_NAME = "bodyMediaType";
    private static final String HTTP_CLIENT_BUILDER_VARIABLE_NAME = "httpClientBuilder";
    private static final String HTTP_CLIENT_VARIABLE_NAME = "httpClient";
    private static final String READ_TIMEOUT_MS_VARIABLE_NAME = "readTimeoutMs";
    private static final String CONNECT_TIMEOUT_MS_VARIABLE_NAME = "connectTimeoutMs";
    private static final String WRITE_TIMEOUT_MS_VARIABLE_NAME = "writeTimeoutMs";
    private static final String RETURN_DESERIALIZER_VARIABLE_NAME = "returnDeserializer";
    private static final String CALL_RESULT_VARIABLE_NAME = "callResult";

    @Override
    public MethodSpec generate(TypeElement classElement, ExecutableElement methodElement,
                               ProcessingEnvironment processingEnv) {
        // 类级别所有注解.
        List<AnnotationDefinition> classAnnotationDefinitions = AnnotationHelper.parseClassAnnotation(classElement);
        // 所有的方法注解定义.
        List<AnnotationDefinition> methodAnnotationDefinitions =
            AnnotationHelper.parseMethodAnnotation(methodElement);
        // 所有的入参注解定义.
        List<AnnotationDefinition> methodParameterAnnotationDefinitions =
            AnnotationHelper.parseMethodParameterAnnotation(methodElement).stream()
                .map(d -> (AnnotationDefinition) d)
                .collect(Collectors.toList());

        // 验证不通过使用默认方法执行.之后不应该出现意外的值.
        if (!verify(methodAnnotationDefinitions, methodParameterAnnotationDefinitions)) {
            return NoSupportGenerationStrategy.getInstance().generate(classElement, methodElement, processingEnv);
        }

        CodeBlock methodCodeBlock = CodeBlock.builder()
            .add(buildPathTemplate(methodAnnotationDefinitions))
            .add(buildPathVariableMapCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildQueryPatamCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildBodyCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildHeadersCodeBlock(methodAnnotationDefinitions))
            .add(buildTimeoutCodeBlock(methodAnnotationDefinitions))
            .add(buildDiscoveryCodeBlock(classAnnotationDefinitions))
            .add(buildReturnDeserializer(methodAnnotationDefinitions))
            .add(buildCallCodeBlock(methodElement))
            .build();

        return MethodGenerationHelper.generateInterfaceMethodImpl(methodElement, methodCodeBlock);
    }

    /*
    检查如下.
    1. Method注解必须存在,且只能有一个.@Get @Post 不能在一个方法上同时存在.
    2. Path注解必须存在,且只能有一个.
    3. 如果 Path 中有变量,那么在入参上必须有 PathVariable 注解.
    4. Timeout 注解只允许一个或者没有.
     */
    private boolean verify(
        List<AnnotationDefinition> methodAnnotationDefinitions,
        List<AnnotationDefinition> methodParameterAnnotationDefinitions) {
        return true;
    }

    private CodeBlock buildCallCodeBlock(ExecutableElement methodElement) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder()
            .addStatement("$T $L = $T.anBuilder()",
                HttpClient.Builder.class, HTTP_CLIENT_BUILDER_VARIABLE_NAME, HttpClient.Builder.class)
            .addStatement("$L.withBaseUrl($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, BASE_URL_VARIABLE_NAME)
            .addStatement("$L.withPathTemplate($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, PATH_TEMPLATE_VARIABLE_NAME)
            .addStatement("$L.withPathVariables($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, PATH_VARIABLE_NAME)
            .addStatement("$L.withQueryParams($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, QUERY_PARAM_VARIABLE_NAME)
            .addStatement("$L.withHeaders($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, HEADER_VARIABLE_NAME)
            .addStatement("$L.withBody($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, BODY_VARIABLE_NAME)
            .addStatement("$L.withBodyMediaType($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, BODY_MEDIA_TYPE_VARIABLE_NAME)
            .addStatement("$L.withReturnDeserializer($L)",
                HTTP_CLIENT_BUILDER_VARIABLE_NAME, RETURN_DESERIALIZER_VARIABLE_NAME)
            .addStatement("$L.withReadTimeoutMs($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, READ_TIMEOUT_MS_VARIABLE_NAME)
            .addStatement("$L.withConnectTimeoutMs($L)",
                HTTP_CLIENT_BUILDER_VARIABLE_NAME, CONNECT_TIMEOUT_MS_VARIABLE_NAME)
            .addStatement("$L.withWriteTimeoutMs($L)",
                HTTP_CLIENT_BUILDER_VARIABLE_NAME, WRITE_TIMEOUT_MS_VARIABLE_NAME)
            .addStatement("$T $L = $L.build()", HttpClient.class, HTTP_CLIENT_VARIABLE_NAME,
                HTTP_CLIENT_BUILDER_VARIABLE_NAME);

        String returnTypeFqn = methodElement.getReturnType().accept(new MethodReturnTypeVisitor(), null);
        codeBlockBuilder.addStatement("$L $L = null", returnTypeFqn, CALL_RESULT_VARIABLE_NAME)
            .beginControlFlow("try");
        codeBlockBuilder.add(finishedCall(HTTP_CLIENT_VARIABLE_NAME, CALL_RESULT_VARIABLE_NAME, returnTypeFqn));
        codeBlockBuilder.nextControlFlow("catch($T | $T e)", IOException.class, ClassNotFoundException.class)
            .addStatement("throw new $T(e.getMessage(), e)", RestfulException.class)
            .endControlFlow()
            .build();

        codeBlockBuilder.addStatement("return $L", CALL_RESULT_VARIABLE_NAME);

        return codeBlockBuilder.build();
    }

    /**
     * 由子类实现.最终的请求代码块.
     *
     * @param httpClientVariableName httpClient的变量名称.
     * @param callResultVariableName httpClient的调用结果存放变量名称.
     * @param returnTypeFqn          调用结果应该的返回结果.
     * @return 执行代码块.
     */
    protected abstract CodeBlock finishedCall(
        String httpClientVariableName, String callResultVariableName, String returnTypeFqn);

    // 这里预期会有一个成员变量 Discovery discovery,并最终生成变量 BASE_URL_VARIABLE_NAME 指定名称的String变量.
    private CodeBlock buildDiscoveryCodeBlock(List<AnnotationDefinition> classAnnotationDefinitions) {
        CodeBlock.Builder builder = CodeBlock.builder();
        String discoveryDefinition =
            getFirstAnnotationDefinition(classAnnotationDefinitions, RestfulClient.class)
                .get()
                .getValue(ANNOTATION_DEFAULT_FIELD_NAME)
                .get()
                .toString();

        if (DiscoveryHelper.isDiscover(discoveryDefinition)) {
            /*
            Optional<Endpoint> endpointOp = discovery.discover(name);
            String baseUrl = null;
            if (endpointOp.isPresent()) {
               Endpoint endpoint = endpointOp.get();
               baseUrl = String.format("%s://%s:%d", endpoint.isTls() ? "https" : "http", endpoint.getHost(), endpoint.getPort());
            } else {
                throws new RestfulException("can not discover");
            }
            */
            builder.addStatement("$T<$T> endpointOp = $L.discover($S)",
                    Optional.class, Endpoint.class,
                    RpcClientProcessor.DISCOVER_MEMBER_VARIABLE_NAME,
                    DiscoveryHelper.parseDiscoverName(discoveryDefinition))
                .addStatement("$T baseUrl = null", String.class)
                .beginControlFlow("if (endpointOp.isPresent())")
                .addStatement("$T endpoint = endpointOp.get()", Endpoint.class)
                .addStatement("baseUrl = "
                    + "String.format(\"%s://%s:%d\",endpoint.isTls() "
                    + "? $S : $S, endpoint.getHost(), endpoint.getPort())", "https", "http")
                .nextControlFlow("else")
                .addStatement("throw new $T($S)",
                    RestfulException.class,
                    String.format(
                        "The service discovery name \"%s\" could not find any valid endpoint.", discoveryDefinition))
                .endControlFlow();
        } else {
            builder.addStatement("$T $L = $S", String.class, BASE_URL_VARIABLE_NAME);
        }

        return builder.build();
    }

    /*
       生成 String pathTemplate = "/{name}/query" 语句.
    */
    private CodeBlock buildPathTemplate(List<AnnotationDefinition> methodAnnotationDefinitions) {
        String pathTemplate =
            (String) getFirstAnnotationDefinition(methodAnnotationDefinitions, Path.class)
                .get()
                .getValue(ANNOTATION_DEFAULT_FIELD_NAME)
                .get();

        return CodeBlock.builder()
            .addStatement("$T pathTemplate = $S", String.class, pathTemplate)
            .build();
    }

    /*
    生成
    Map<String, String> pathVariableMap = new HashMap<>();
    pathVariableMap.put("{设定的变量名称}", {序列化类型全限定名称}.getInstance({入参参数名称}));
    ....
    pathVariableMap.put("{设定的变量名称}", {序列化类型全限定名称}.getInstance({入参参数名称}));
     */
    private CodeBlock buildPathVariableMapCodeBlock(
        List<AnnotationDefinition> methodParameterAnnotationDefinitions) {

        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (haveAnnotation(methodParameterAnnotationDefinitions, PathVariable.class)) {
            codeBlockBuilder.addStatement(
                "$T<String, String> $L = new $T<>()", Map.class, PATH_VARIABLE_NAME, HashMap.class);
            getAllAnnotationDefinitions(methodParameterAnnotationDefinitions, PathVariable.class)
                .stream()
                .map(d -> (ParameterAnnotationDefinition) d)
                .forEach(d -> {
                    String pathVariableName = d.getValue(ANNOTATION_DEFAULT_FIELD_NAME).get().equals("")
                        ? d.getParameterName() : (String) d.getValue(ANNOTATION_DEFAULT_FIELD_NAME).get();


                    codeBlockBuilder.addStatement(
                        "$L.put($S, $L.getInstance().serialize($L))",
                        PATH_VARIABLE_NAME,
                        pathVariableName,
                        d.getValue(ANNOTATION_SERIALIZER_FIELD_NAME).get(),
                        d.getParameterName());
                });
        } else {
            codeBlockBuilder.addStatement(
                "$T<String, String> $L = $L.emptyMap()", Map.class, PATH_VARIABLE_NAME, Collections.class.getName());
        }

        return codeBlockBuilder.build();
    }

    /*
    生成
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("{设定的变量名称}", {序列化类型全限定名称}.getInstance({入参参数名称}));
    ...
    queryParams.put("{设定的变量名称}", {序列化类型全限定名称}.getInstance({入参参数名称}));
     */
    private CodeBlock buildQueryPatamCodeBlock(
        List<AnnotationDefinition> methodParameterAnnotationDefinitions) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        if (haveAnnotation(methodParameterAnnotationDefinitions, QueryParam.class)) {
            codeBlockBuilder.addStatement(
                "$T<String, String> $L = new $T<>()", Map.class, QUERY_PARAM_VARIABLE_NAME, HashMap.class);
            getAllAnnotationDefinitions(methodParameterAnnotationDefinitions, QueryParam.class)
                .stream()
                .map(d -> (ParameterAnnotationDefinition) d)
                .forEach(d -> {
                    String queryParamName = d.getValue(ANNOTATION_DEFAULT_FIELD_NAME).get().equals("")
                        ? d.getParameterName() : (String) d.getValue(ANNOTATION_DEFAULT_FIELD_NAME).get();

                    codeBlockBuilder.addStatement(
                        "$L.put($S, $L.getInstance().serialize($L))",
                        QUERY_PARAM_VARIABLE_NAME,
                        queryParamName,
                        d.getValue(ANNOTATION_SERIALIZER_FIELD_NAME).get(),
                        d.getParameterName());
                });
        } else {
            codeBlockBuilder.addStatement(
                "$T<String, String> $L = $L.emptyMap()",
                Map.class,
                QUERY_PARAM_VARIABLE_NAME,
                Collections.class.getName());
        }

        return codeBlockBuilder.build();
    }

    /*
    生成如下代码.
    byte[] body = {序列化器}.getInstance({入参参数名称});
    String bodyMediaType = "{注解指定的媒体类型字符串}";
     */
    private CodeBlock buildBodyCodeBlock(
        List<AnnotationDefinition> methodParameterAnnotationDefinitions) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

        if (haveAnnotation(methodParameterAnnotationDefinitions, Body.class)) {

            getAllAnnotationDefinitions(methodParameterAnnotationDefinitions, Body.class)
                .stream()
                .map(d -> (ParameterAnnotationDefinition) d)
                .findFirst().ifPresent(d -> {
                    codeBlockBuilder.addStatement(
                        "$T $L = $L.getInstance().serialize($L))",
                        Object.class, BODY_VARIABLE_NAME,
                        d.getValue(ANNOTATION_SERIALIZER_FIELD_NAME).get(),
                        d.getParameterName());
                    codeBlockBuilder.addStatement(
                        "$T $L = $L",
                        String.class,
                        BODY_MEDIA_TYPE_VARIABLE_NAME,
                        d.getValue("mediaType"),
                        d.getParameterName());
                });
        } else {
            codeBlockBuilder.addStatement("$T $L = null", Object.class, BODY_VARIABLE_NAME);
            codeBlockBuilder.addStatement("$T $L = null", String.class, BODY_MEDIA_TYPE_VARIABLE_NAME);
        }

        return codeBlockBuilder.build();
    }

    /*
    生成如下代码.
    List<Map.Entry<String, String>> headers = new ArrayList<>();
    headers.add(new SimpleEntry<>("{key}", "{val}"));
    ....
    headers.add(new SimpleEntry<>("{key}", "{val}"));
     */
    private CodeBlock buildHeadersCodeBlock(List<AnnotationDefinition> methodAnnotationDefinitions) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (haveAnnotation(methodAnnotationDefinitions, Header.class)) {
            codeBlockBuilder.addStatement(
                "$T<$T<$T, $T>> $L = new $T<>()",
                List.class, Map.Entry.class, String.class, String.class, HEADER_VARIABLE_NAME, Arrays.class);
            getAllAnnotationDefinitions(methodAnnotationDefinitions, Header.class).forEach(d -> {
                String key = (String) d.getValue("key").orElse("");
                String value = (String) d.getValue("val").orElse("");
                if (!isStringNullOrEmpty(key)) {
                    codeBlockBuilder.addStatement("$L.add(new $L($S, $S))", HEADER_VARIABLE_NAME,
                        AbstractMap.SimpleEntry.class.getName(), key, value);
                }
            });
        } else {
            codeBlockBuilder.addStatement(
                "$T<$T<$T, $T>> $L = $L.emptyList()",
                List.class,
                Map.Entry.class,
                String.class,
                String.class,
                HEADER_VARIABLE_NAME,
                Collections.class.getName());
        }
        return codeBlockBuilder.build();
    }

    private CodeBlock buildTimeoutCodeBlock(List<AnnotationDefinition> methodAnnotationDefinitions) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (haveAnnotation(methodAnnotationDefinitions, Timeout.class)) {
            AnnotationDefinition definition =
                getFirstAnnotationDefinition(methodAnnotationDefinitions, Timeout.class).get();

            return codeBlockBuilder
                .addStatement("$T $L = $L", long.class, READ_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("readTimeoutMs"))
                .addStatement("$T $L = $L", long.class, WRITE_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("writeTimeoutMs"))
                .addStatement("$T $L = $L", long.class, CONNECT_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("connectTimeoutMs"))
                .build();

        } else {
            long defaultReadTimeoutMs = 0;
            long defaultWriteTimeoutMs = 0;
            long defaultConnectTimeoutMs = 0;
            try {
                defaultReadTimeoutMs = (long) Timeout.class.getMethod("readTimeoutMs").getDefaultValue();
                defaultWriteTimeoutMs = (long) Timeout.class.getMethod("writeTimeoutMs").getDefaultValue();
                defaultConnectTimeoutMs = (long) Timeout.class.getMethod("connectTimeoutMs").getDefaultValue();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            return codeBlockBuilder
                .addStatement("$T $L = $L", long.class, READ_TIMEOUT_MS_VARIABLE_NAME, defaultReadTimeoutMs)
                .addStatement("$T $L = $L", long.class, WRITE_TIMEOUT_MS_VARIABLE_NAME, defaultWriteTimeoutMs)
                .addStatement("$T $L = $L", long.class, CONNECT_TIMEOUT_MS_VARIABLE_NAME, defaultConnectTimeoutMs)
                .build();
        }
    }

    private CodeBlock buildReturnDeserializer(List<AnnotationDefinition> methodAnnotationDefinitions) {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (haveAnnotation(methodAnnotationDefinitions, ReturnDeserializer.class)) {
            AnnotationDefinition definition =
                getFirstAnnotationDefinition(methodAnnotationDefinitions, ReturnDeserializer.class).get();

            Optional<?> value = definition.getValue(ANNOTATION_DEFAULT_FIELD_NAME);
            if (value.isPresent()) {
                return builder
                    .addStatement("$T $L = $L.getInstance()",
                        BytesDeserializer.class,
                        RETURN_DESERIALIZER_VARIABLE_NAME,
                        value.get())
                    .build();
            }

        }

        // 没有设置,让框架自己选择.
        return builder
            .addStatement("$T $L = null", BytesDeserializer.class, RETURN_DESERIALIZER_VARIABLE_NAME)
            .build();
    }

    private boolean isStringNullOrEmpty(String val) {
        return val == null || val.isEmpty();
    }

    private long getAnnotationSize(
        List<AnnotationDefinition> annotationDefinitions, Class<?> annotationClass) {
        return annotationDefinitions
            .stream()
            .filter(d -> d.getFqn().equals(annotationClass.getName()))
            .count();
    }

    private boolean haveAnnotation(
        List<AnnotationDefinition> annotationDefinitions, Class<?> annotationClass) {
        return getAnnotationSize(annotationDefinitions, annotationClass) > 0;
    }

    // 获取指定类型的第一个注解,如果没有将返回null.
    private Optional<AnnotationDefinition> getFirstAnnotationDefinition(
        List<AnnotationDefinition> annotationDefinitions, Class<?> annotationClass) {
        return annotationDefinitions
            .stream()
            .filter(d -> d.getFqn().equals(annotationClass.getName()))
            .findFirst();
    }

    private List<AnnotationDefinition> getAllAnnotationDefinitions(
        List<AnnotationDefinition> annotationDefinitions, Class<?> annotationClass) {
        return annotationDefinitions
            .stream()
            .filter(d -> d.getFqn().equals(annotationClass.getName()))
            .collect(Collectors.toList());
    }

    static class MethodReturnTypeVisitor implements TypeVisitor<String, Void> {

        @Override
        public String visit(TypeMirror t, Void unused) {
            return t.accept(this, unused);
        }

        @Override
        public String visit(TypeMirror t) {
            return t.accept(this, null);
        }

        @Override
        public String visitPrimitive(PrimitiveType t, Void unused) {
            return t.toString();
        }

        @Override
        public String visitNull(NullType t, Void unused) {
            return "null";
        }

        @Override
        public String visitArray(ArrayType t, Void unused) {
            return t.getComponentType().accept(this, null) + "[]";
        }

        @Override
        public String visitDeclared(DeclaredType t, Void unused) {
            Element el = t.asElement();
            if (el instanceof TypeElement) {
                return ((TypeElement) el).getQualifiedName().toString();
            }
            return t.toString();
        }

        @Override
        public String visitError(ErrorType t, Void unused) {
            return t.toString();
        }

        @Override
        public String visitTypeVariable(TypeVariable t, Void unused) {
            Element el = t.asElement();
            if (el instanceof TypeParameterElement) {
                TypeMirror upperBound = t.getUpperBound();
                // 如果有界，递归获取边界的完全限定名
                if (upperBound != null) {
                    return upperBound.accept(this, null);
                }
            }
            return t.toString();
        }

        @Override
        public String visitWildcard(WildcardType t, Void unused) {
            StringBuilder sb = new StringBuilder("?");
            TypeMirror extendsBound = t.getExtendsBound();
            if (extendsBound != null) {
                sb.append(" extends ").append(extendsBound.accept(this, null));
            }
            TypeMirror superBound = t.getSuperBound();
            if (superBound != null) {
                sb.append(" super ").append(superBound.accept(this, null));
            }
            return sb.toString();
        }

        @Override
        public String visitExecutable(ExecutableType t, Void unused) {
            return t.getReturnType().accept(this, null);
        }

        @Override
        public String visitNoType(NoType t, Void unused) {
            if (t.getKind() == TypeKind.VOID) {
                return "void";
            }
            return t.toString();
        }

        @Override
        public String visitUnknown(TypeMirror t, Void unused) {
            throw new UnknownTypeException(t, null);
        }

        @Override
        public String visitUnion(UnionType t, Void unused) {
            return t.getAlternatives().stream()
                .map(alternativeType -> alternativeType.accept(this, null))
                .reduce((s1, s2) -> s1 + " | " + s2)
                .orElse("");
        }

        @Override
        public String visitIntersection(IntersectionType t, Void unused) {
            return t.getBounds().stream()
                .map(boundType -> boundType.accept(this, null))
                .reduce((s1, s2) -> s1 + " & " + s2)
                .orElse("");
        }
    }
}

