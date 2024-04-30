package com.vmsmia.framework.component.rpc.restful.standard.generation;

import static com.vmsmia.framework.component.rpc.restful.standard.RpcClientProcessor.DISCOVER_MEMBER_VARIABLE_NAME;
import static com.vmsmia.framework.component.rpc.restful.standard.RpcClientProcessor.LOAD_BALANCER_VARIABLE_NAME;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.vmsmia.framework.component.rpc.restful.annotation.Path;
import com.vmsmia.framework.component.rpc.restful.annotation.RequestHead;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.annotation.ReturnDeserializer;
import com.vmsmia.framework.component.rpc.restful.annotation.Timeout;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Delete;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Get;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Head;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Patch;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Post;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Put;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Stream;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.Body;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.PathVariable;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.QueryParam;
import com.vmsmia.framework.component.rpc.restful.common.exception.RestfulException;
import com.vmsmia.framework.component.rpc.restful.discovery.Discovery;
import com.vmsmia.framework.component.rpc.restful.discovery.Endpoint;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancerProcesser;
import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.standard.RpcClientProcessor;
import com.vmsmia.framework.component.rpc.restful.standard.client.HttpClient;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationDefinition;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationHelper;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.MethodGenerationHelper;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.ParameterAnnotationDefinition;
import com.vmsmia.framework.component.rpc.restful.stream.StreamSubscriber;
import java.util.AbstractMap;
import java.util.ArrayList;
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
import javax.lang.model.element.VariableElement;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * {@code AbstractMethodGenerationStrategy} 类是一个抽象类，实现了 {@code MethodGenerationStrategy} 接口.
 * 它提供了生成 RPC 接口方法实现的基础框架和共用逻辑.子类通过扩展此类并实现特定的逻辑，可以灵活地生成不同风格
 * 的 HTTP 请求处理代码.
 * <p>
 * 此类中定义了一系列用于构建方法实现所需的代码片段的方法，例如路径参数处理、查询参数处理、请求体处理、头信息处理等.
 * 它利用注解定义{@link AnnotationDefinition}来解析接口和方法上的注解信息，并基于这些信息构建出相应的 OkHttp 调用代码.
 * </p>
 *
 * <h3>关键属性</h3>
 * 每个属性对应一个特定HTTP请求的组成部分或请求构造过程中需要的配置信息，如基本URL、路径模板、路径变量、查询参数等.
 * <ul>
 *     <li>{@code ANNOTATION_DEFAULT_FIELD_NAME} - 指向注解中默认值的属性名称.</li>
 *     <li>{@code ANNOTATION_SERIALIZER_FIELD_NAME} - 指向进行序列化操作的注解属性名称.</li>
 *     <li>其余属性则分别对应 HTTP 请求过程中的基本要素，如{@code baseUrl}, {@code pathTemplate}, 或是各类timeout配置.</li>
 * </ul>
 *
 * <h3>核心方法</h3>
 * <ul>
 *     <li>{@code generate} - 实现{@code MethodGenerationStrategy}接口的主方法，负责生成接口方法的实现.</li>
 *     <li>{@code verify} - 对接口方法及其注解进行校验，确保满足生成代码所需的基本条件.</li>
 *     <li>{@code buildCallCodeBlock} - 构建实现中对 HTTP 客户端发起请求的核心代码块.</li>
 *     <li>{@code finishedCall} - 需要由子类实现的抽象方法，用于生成最终的 HTTP 请求调用代码.</li>
 *     <li>其他以 {@code build} 开头的方法分别负责构建方法实现中的不同部分，例如请求体，查询参数等.</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <p>此抽象类主要用于在编译时通过注解处理器自动为标记了特定注解的接口生成基于 OkHttp 的客户端代码.
 * 开发者可以扩展此类并实现 {@code finishedCall} 方法来自定义 HTTP 请求的构造和处理逻辑，满足不同的业务需求.</p>
 *
 * <h3>扩展指南</h3>
 * <p>要创建自己的方法生成策略，继承此类并实现 {@code finishedCall} 方法即可.你可能还想重写 {@code verify} 方法
 * 来提供额外的校验逻辑，确保你的注解使用正确.</p>
 *
 * @author bin.dong
 * @version 0.1 2024/4/10 15:19
 * @see MethodGenerationStrategy
 * @see AnnotationDefinition
 * @since 1.8
 */
public abstract class AbstractMethodGenerationStrategy implements MethodGenerationStrategy {

    /**
     * 注解序列化属性的名称.
     */
    private static final String ANNOTATION_SERIALIZER_FIELD_NAME = "serializer";

    private static final String ENDPOINT_VARIABLE_NAME = "endpoint";
    private static final String PATH_TEMPLATE_VARIABLE_NAME = "pathTemplate";
    private static final String PATH_VARIABLE_NAME = "pathVariables";
    private static final String QUERY_PARAM_VARIABLE_NAME = "queryParams";
    private static final String HEAD_VARIABLE_NAME = "heads";
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
    public MethodSpec generate(TypeElement interfaceElement, ExecutableElement methodElement,
                               ProcessingEnvironment processingEnv) {
        // 类级别所有注解.
        List<AnnotationDefinition> classAnnotationDefinitions = AnnotationHelper.parseClassAnnotation(interfaceElement);
        // 所有的方法注解定义.
        List<AnnotationDefinition> methodAnnotationDefinitions =
            AnnotationHelper.parseMethodAnnotation(methodElement);
        // 所有的入参注解定义.
        List<AnnotationDefinition> methodParameterAnnotationDefinitions =
            AnnotationHelper.parseMethodParameterAnnotation(methodElement).stream()
                .map(d -> (AnnotationDefinition) d)
                .collect(Collectors.toList());

        // 验证不通过使用默认方法执行.之后不应该出现意外的值.
        if (!verify(
            interfaceElement,
            methodElement,
            methodAnnotationDefinitions,
            processingEnv)) {
            return null;
        }

        boolean stream = haveAnnotation(methodAnnotationDefinitions, Stream.class);
        String returnTypeFqn = methodElement.getReturnType().accept(new MethodReturnTypeVisitor(), null);

        CodeBlock methodCodeBlock = CodeBlock.builder()
            .add(buildPathTemplate(methodAnnotationDefinitions))
            .add(buildPathVariableMapCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildQueryPatamCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildBodyCodeBlock(methodParameterAnnotationDefinitions))
            .add(buildHeadersCodeBlock(methodAnnotationDefinitions))
            .add(buildTimeoutCodeBlock(methodAnnotationDefinitions))
            .add(buildReturnDeserializer(methodAnnotationDefinitions))
            .add(buildDiscoveryCodeBlock(classAnnotationDefinitions, stream, returnTypeFqn,
                buildCallCodeBlock(methodElement, processingEnv, stream, returnTypeFqn)))
            .build();

        return MethodGenerationHelper.generateInterfaceMethodImpl(methodElement, methodCodeBlock);
    }

    /*
    检查如下.
    1. Method注解必须存在,且只能有一个.@Get @Post 不能在一个方法上同时存在.
    2. Path注解必须存在,且只能有一个.
    3. Timeout 只允许0个或者1个.
    4. ReturnDeserializer 只允许0个或者1个.
    5. 如果是Stream的method,那么返回值必须是void且必须有且只能有一个入参是实现了StreamSubscriber接口.
    6. 如果是head请求,那么方法响应值必须是List<Map.Entry<String, String>>类型.
     */
    protected boolean verify(
        TypeElement interfaceElement,
        ExecutableElement methodElement,
        List<AnnotationDefinition> methodAnnotationDefinitions,
        ProcessingEnvironment processingEnv) {
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();
        /*
        规则: @Get @Post @Put @Delete @Patch @Head @Stream 不能同时存在.
        如下违反规则.
        @Get
        @Post
         */
        if (methodAnnotationDefinitions.stream()
            .filter(d -> d.getFqn().equals(Get.class.getName())
                || d.getFqn().equals(Delete.class.getName())
                || d.getFqn().equals(Post.class.getName())
                || d.getFqn().equals(Put.class.getName())
                || d.getFqn().equals(Patch.class.getName())
                || d.getFqn().equals(Head.class.getName())).count() > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                String.format(
                    "[%s.%s] The Method annotation must exist, and only one must exist."
                        + "[@Get, @Post, @Put, @Delete, @Patch, @Head, @Stream] cannot exist at the same time.",
                    interfaceElement.getQualifiedName().toString(), methodElement.getSimpleName().toString()
                ));
            return false;
        }
        /*
        规则: @Path 必须存在,且只能有一个.
        如下违反规则.
        @Path("/p1")
        @Path("/p2")
         */
        long pathSize = getAnnotationSize(methodAnnotationDefinitions, Path.class);
        if (pathSize != 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                String.format("[%s] The Path annotation must exist, and only one must exist.",
                    interfaceElement.getQualifiedName().toString()));
            return false;
        }

        /*
        规则: Timeout 只允许0个或者1个.
        如下违反规则.
        @Timeout(1000)
        @Timeout(1000)
         */
        long timeoutSize = getAnnotationSize(methodAnnotationDefinitions, Timeout.class);
        if (timeoutSize > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                String.format("[%s] The Timeout annotation can only be 0 or 1.",
                    interfaceElement.getQualifiedName().toString()));
            return false;
        }

        /*
        规则: ReturnDeserializer 只允许0个或者1个.
        如下违反规则.
        @ReturnDeserializer(StringDeserializer.class)
        @ReturnDeserializer(StringDeserializer.class)
         */
        long returnDeserializerSize = getAnnotationSize(methodAnnotationDefinitions, ReturnDeserializer.class);
        if (returnDeserializerSize > 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                String.format("[%s] The ReturnDeserializer annotation can only be 0 or 1.",
                    interfaceElement.getQualifiedName().toString()));
            return false;
        }

        /*
        规则: 如果是Stream的method,那么返回值必须是void且必须有且只能有一个入参是实现了StreamSubscriber接口.
         */
        if (haveAnnotation(methodAnnotationDefinitions, Stream.class)) {
            if (TypeKind.VOID != methodElement.getReturnType().getKind()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format("[%s]The method definition for streaming requests can only be void..",
                        interfaceElement.getQualifiedName().toString()));
                return false;
            }

            TypeMirror streamSubscriberTypeMirror =
                elements.getTypeElement(StreamSubscriber.class.getCanonicalName()).asType();
            long subscriberSize = methodElement.getParameters().stream()
                .filter(p -> {
                    TypeMirror paramTypeMirror = p.asType();
                    if (paramTypeMirror.getKind() == TypeKind.DECLARED) {
                        return types.isAssignable(paramTypeMirror, streamSubscriberTypeMirror);
                    }

                    return false;
                }).count();

            final long onlyOneStreamSubscriber = 1;
            if (subscriberSize != onlyOneStreamSubscriber) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format(
                        "[%s]The method definition for streaming requests can only have one parameter "
                            + "that implements the StreamSubscriber interface.",
                        interfaceElement.getQualifiedName().toString()));
                return false;
            }
        }

        /*
        规则: 如果是head请求,那么方法响应值必须是List<Map.Entry<String, String>>类型.
         */
        if (haveAnnotation(methodAnnotationDefinitions, Head.class)) {
            TypeMirror returnTypeMirror = methodElement.getReturnType();
            TypeMirror listTypeMirror = elements.getTypeElement(List.class.getCanonicalName()).asType();
            if (!types.isAssignable(types.erasure(returnTypeMirror), types.erasure(listTypeMirror))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format(
                        "[%s]The method definition for head requests can only be List.",
                        interfaceElement.getQualifiedName().toString()));
                return false;
            }

            long size = ((DeclaredType) returnTypeMirror).getTypeArguments().stream().filter(arg ->
                types.isAssignable(arg, types.getDeclaredType(
                    elements.getTypeElement(Map.Entry.class.getCanonicalName()),
                    elements.getTypeElement(String.class.getCanonicalName()).asType(),
                    elements.getTypeElement(String.class.getCanonicalName()).asType()
                ))).count();
            if (size != 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    String.format(
                        "[%s]The method definition for head requests can only be List<Map.Entry<String, String>.",
                        interfaceElement.getQualifiedName().toString()));
                return false;
            }

        }

        return true;
    }

    // 这里预期会有二个成员变量 Discovery discovery,和一个LoadBlancer loadBalancer.
    private CodeBlock buildDiscoveryCodeBlock(List<AnnotationDefinition> classAnnotationDefinitions,
                                              boolean stream,
                                              String returnTypeFqn,
                                              CodeBlock callCode) {
        CodeBlock.Builder builder = CodeBlock.builder();
        // 注解机制决定了RestfulClient一定存在且一定有value值.
        String discoverDefinition =
            getFirstAnnotationDefinition(classAnnotationDefinitions, RestfulClient.class)
                .get()
                .getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME)
                .get()
                .toString();

        if (Discovery.isDiscover(discoverDefinition)) {
            String serviceName = Discovery.parseDiscoverName(discoverDefinition);

            /*
            目标生成如下基于服务发现的执行代码,重点是使用try-with-resources方式保证负载均衡开启和结束的成对.

            Optional<LoadBalancerSelected> selected = loadBalancer.select(discovery.discover(serviceName));
            if (selected.isPresent()) {
                try (LoadBalancerSelected loadBalancerSelected = selected.get()) {
                    Endpoint endpoint = loadBalancerSelected.endpoint();

                    // 请求执行代码并返回结果

                } catch (Exception ex) {
                    throw new RestfulException(ex.getMessage(), ex);
                }

            } else {
                throw new RestfulException(String.format("Can not discover %s", serviceName));
            }
             */
            if (!stream) {
                builder.add("return ($L) $T.processWithReturn($L, $L, $S, ($L) -> {\n",
                        returnTypeFqn,
                        LoadBalancerProcesser.class,
                        DISCOVER_MEMBER_VARIABLE_NAME,
                        LOAD_BALANCER_VARIABLE_NAME,
                        serviceName,
                        ENDPOINT_VARIABLE_NAME)
                    .indent()
                    .add(callCode)
                    .unindent()
                    .addStatement("})");
            } else {
                builder.add("$T.processWithNoReturn($L, $L, $S, ($L) -> {\n",
                        LoadBalancerProcesser.class,
                        DISCOVER_MEMBER_VARIABLE_NAME,
                        LOAD_BALANCER_VARIABLE_NAME,
                        serviceName,
                        ENDPOINT_VARIABLE_NAME)
                    .indent()
                    .add(callCode)
                    .unindent()
                    .addStatement("})");
            }

        } else {
            // 不进行服务发现直接访问.
            builder
                .addStatement("$T $L = $T.parse($S)",
                    Endpoint.class, ENDPOINT_VARIABLE_NAME, Endpoint.class, discoverDefinition)
                .add(callCode);
        }

        return builder.build();
    }

    // 实际产生请求的代码.
    private CodeBlock buildCallCodeBlock(ExecutableElement methodElement,
                                         ProcessingEnvironment processingEnv,
                                         boolean stream,
                                         String returnTypeFqn) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        // 构建HttpClient并执行.
        codeBlockBuilder.addStatement("$T $L = $T.anBuilder()",
                HttpClient.Builder.class, HTTP_CLIENT_BUILDER_VARIABLE_NAME, HttpClient.Builder.class)
            .addStatement("$L.withOkHttpClient($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME,
                RpcClientProcessor.OKHTTPCLIENT_MEMBER_VARIABLE_NAME)
            .addStatement("$L.withEndpoint($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, ENDPOINT_VARIABLE_NAME)
            .addStatement("$L.withPathTemplate($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, PATH_TEMPLATE_VARIABLE_NAME)
            .addStatement("$L.withPathVariables($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, PATH_VARIABLE_NAME)
            .addStatement("$L.withQueryParams($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, QUERY_PARAM_VARIABLE_NAME)
            .addStatement("$L.withHeaders($L)", HTTP_CLIENT_BUILDER_VARIABLE_NAME, HEAD_VARIABLE_NAME)
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

        if (stream) {
            // 找到第一个可以找到的StreamSubscriber接口实现入参.
            // 校验已经确认了一定会有一个这样的入参.
            Elements elements = processingEnv.getElementUtils();
            Types types = processingEnv.getTypeUtils();
            TypeMirror streamSubscriberTypeMirror =
                elements.getTypeElement(StreamSubscriber.class.getCanonicalName()).asType();
            VariableElement streamParameterElement = methodElement.getParameters()
                .stream()
                .filter(ve -> types.isAssignable(ve.asType(), streamSubscriberTypeMirror))
                .findFirst().get();

            codeBlockBuilder.beginControlFlow("try")
                .add(finishedCall(HTTP_CLIENT_VARIABLE_NAME, streamParameterElement.getSimpleName().toString(), null))
                .nextControlFlow("catch($T e)", Exception.class)
                .addStatement("throw new $T(e.getMessage(), e)", RestfulException.class)
                .endControlFlow();

        } else {
            codeBlockBuilder.addStatement("$L $L = null", returnTypeFqn, CALL_RESULT_VARIABLE_NAME)
                .beginControlFlow("try");
            codeBlockBuilder.add(finishedCall(HTTP_CLIENT_VARIABLE_NAME, CALL_RESULT_VARIABLE_NAME, returnTypeFqn));
            codeBlockBuilder.nextControlFlow("catch($T e)", Exception.class)
                .addStatement("throw new $T(e.getMessage(), e)", RestfulException.class)
                .endControlFlow();

            codeBlockBuilder.addStatement("return $L", CALL_RESULT_VARIABLE_NAME);
        }

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

    /*
       生成 String pathTemplate = "/{name}/query" 语句.

    */
    private CodeBlock buildPathTemplate(List<AnnotationDefinition> methodAnnotationDefinitions) {
        // 规则检查决定了必然有@Path.
        String pathTemplate =
            (String) getFirstAnnotationDefinition(methodAnnotationDefinitions, Path.class)
                .get()
                .getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME)
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
                    String pathVariableName = d.getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME).get().equals("")
                        ? d.getParameterName() : (String) d.getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME).get();


                    codeBlockBuilder.addStatement(
                        "$L.put($S, $L.getInstance().serialize($L))",
                        PATH_VARIABLE_NAME,
                        pathVariableName,
                        // @PathVariable 会有默认值.
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
                    String annotationValue = (String) d.getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME).get();
                    String queryParamName = annotationValue.isEmpty() ? d.getParameterName() : annotationValue;

                    codeBlockBuilder.addStatement(
                        "$L.put($S, $L.getInstance().serialize($L))",
                        QUERY_PARAM_VARIABLE_NAME,
                        queryParamName,
                        // @QueryParam 会有 serializer 默认值.
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
                        // @Body 有 serializer 默认值.
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
        if (haveAnnotation(methodAnnotationDefinitions, RequestHead.class)) {
            codeBlockBuilder.addStatement(
                "$T<$T<$T, $T>> $L = new $T<>()",
                List.class, Map.Entry.class, String.class, String.class, HEAD_VARIABLE_NAME, ArrayList.class);
            getAllAnnotationDefinitions(methodAnnotationDefinitions, RequestHead.class).forEach(d -> {
                String key = (String) d.getValue("key").orElse("");
                String value = (String) d.getValue("val").orElse("");
                if (!isStringNullOrEmpty(key)) {
                    codeBlockBuilder.addStatement("$L.add(new $T($S, $S))", HEAD_VARIABLE_NAME,
                        AbstractMap.SimpleEntry.class, key, value);
                }
            });
        } else {
            codeBlockBuilder.addStatement(
                "$T<$T<$T, $T>> $L = $L.emptyList()",
                List.class,
                Map.Entry.class,
                String.class,
                String.class,
                HEAD_VARIABLE_NAME,
                Collections.class.getName());
        }
        return codeBlockBuilder.build();
    }

    private CodeBlock buildTimeoutCodeBlock(List<AnnotationDefinition> methodAnnotationDefinitions) {
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (haveAnnotation(methodAnnotationDefinitions, Timeout.class)) {
            AnnotationDefinition definition =
                getFirstAnnotationDefinition(methodAnnotationDefinitions, Timeout.class).get();

            /*
            Timeout标准的值都有默认值,所以可以安全的Optional.get()调用.
             */
            return codeBlockBuilder
                .addStatement("$T $L = $L", long.class, READ_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("readTimeoutMs").get())
                .addStatement("$T $L = $L", long.class, WRITE_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("writeTimeoutMs").get())
                .addStatement("$T $L = $L", long.class, CONNECT_TIMEOUT_MS_VARIABLE_NAME,
                    definition.getValue("connectTimeoutMs").get())
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

            Optional<?> value = definition.getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME);
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

    private long getAnnotationSize(List<AnnotationDefinition> methodAnnotationDefinitions, Class<?> clazz) {
        return methodAnnotationDefinitions.stream()
            .filter(d -> d.getFqn().equals(clazz.getName())).count();
    }

    private boolean haveAnnotation(
        List<AnnotationDefinition> annotationDefinitions, Class<?> annotationClass) {
        return annotationDefinitions.stream()
            .anyMatch(d -> d.getFqn().equals(annotationClass.getName()));
    }

    // 获取指定类型的第一个注解.
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

