package com.vmsmia.framework.component.rpc.restful.standard;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.discovery.Discovery;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancer;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancerFactory;
import com.vmsmia.framework.component.rpc.restful.loadbalancer.LoadBalancers;
import com.vmsmia.framework.component.rpc.restful.standard.generation.MethodGenerationStrategy;
import com.vmsmia.framework.component.rpc.restful.standard.generation.MethodGenerationStrategyFactory;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationDefinition;
import com.vmsmia.framework.component.rpc.restful.standard.generation.helper.AnnotationHelper;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import okhttp3.OkHttpClient;

/**
 * RpcClientProcessor是一个自定义的注解处理器，专门用于处理
 * {@code com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient}注解.
 * 它在编译时自动触发，生成目标接口的实现类，进而支持RPC调用的简化和自动化.
 * <p>
 * 当一个接口使用了{@code RestfulClient}注解后，此处理器将为该接口生成一个具体的实现类，该实现类包含了通过HTTP客户端调用远程服务的必要逻辑.
 * 生成的实现类包含对okhttpclient和服务发现机制的引用，以便实现动态的服务调用.
 * </p>
 *
 * <h2>使用说明</h2>
 * <ol>
 *     <li>将 {@code @RestfulClient} 应用于任何接口定义</li>
 *     <li>编译项目时，注解处理器自动运行，并为每个标注的接口生成实现类</li>
 *     <li>生成的类会在指定的包下(RpcClientProcessor.GENERATION_PACKAGE)，类名默认为接口名加Impl后缀，也可通过配置修改</li>
 * </ol>
 *
 * <h2>配置项</h2>
 * 此处理器允许通过注解处理器选项进行配置，目前支持的配置项包括：
 * <ul>
 *     <li>{@code useFixedClassName}: 决定生成的类名是否固定.若为true，则遵循简单命名规则{@code {接口名}Impl}；
 *     若为false，则生成的类名会加上目标接口包名去除"."后的字符串做为后缀，以确保唯一性.</li>
 * </ul>
 *
 * <h2>类成员简介</h2>
 * <ul>
 *     <li>{@code GENERATION_PACKAGE}: 生成代码的目标包名</li>
 *     <li>{@code DISCOVER_MEMBER_VARIABLE_NAME}: 用于服务发现的成员变量名</li>
 *     <li>{@code OKHTTPCLIENT_MEMBER_VARIABLE_NAME}: 用于HTTP请求的okhttpclient成员变量名</li>
 *     <li>{@code filer}: 文件生成器，用于创建新文件</li>
 *     <li>{@code messager}: 用于在编译期间打印信息</li>
 *     <li>{@code types}: 类型工具类，用于类型处理</li>
 *     <li>{@code elements}: 元素工具类，用于操作元素</li>
 *     <li>{@code configuration}: 处理器配置信息</li>
 * </ul>
 *
 * <h2>关键方法</h2>
 * <ul>
 *    <li>{@code init}: 初始化处理器，获取必要的处理环境工具</li>
 *    <li>{@code process}: 主要处理方法，扫描带有{@code RestfulClient}注解的接口，并为每个接口生成实现类</li>
 *    <li>{@code generateImpl}: 为指定接口生成实现类定义</li>
 *    <li>{@code generateDiscoverField}: 生成服务发现机制的字段定义</li>
 *    <li>{@code generateOkHttpClientField}: 生成okhttpclient成员变量的字段定义</li>
 *    <li>{@code collectInterfaceMethods}: 收集接口及其父接口中的所有方法</li>
 * </ul>
 *
 * <h2>注意事项</h2>
 * 生成的实现类代码不应手动修改，因为它们在编译时自动生成，且有可能在之后的编译中被覆盖.
 *
 * @author bin.dong
 * @version 0.1 2024/4/8 16:20
 * @since 1.8
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RpcClientProcessor extends AbstractProcessor {

    /**
     * 生成代码的目标包.
     */
    public static final String GENERATION_PACKAGE = "com.vmsmia.framework.component.rpc.restful.standard.generation";
    /**
     * 负载均衡器成员变量名称.
     */
    public static final String LOAD_BALANCER_VARIABLE_NAME = "loadBalancer";
    /**
     * 服务发现成员变量名称.
     */
    public static final String DISCOVER_MEMBER_VARIABLE_NAME = "discovery";
    /**
     * okhttpclient成员变量名称.
     */
    public static final String OKHTTPCLIENT_MEMBER_VARIABLE_NAME = "okHttpClient";
    private Filer filer;
    private Messager messager;
    private Types types;
    private Elements elements;
    private Configuration configuration;
    private String version;
    private String jdkVersion;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.types = processingEnv.getTypeUtils();
        this.configuration = new Configuration(processingEnv.getOptions());
        this.version = readVersion();
        this.jdkVersion = readJdkVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> els = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element el : els) {
                if (el.getKind() == ElementKind.INTERFACE) {
                    TypeElement interfaceEl = (TypeElement) el;
                    TypeSpec classSpec = generateImpl(interfaceEl);

                    messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        String.format(
                            "Generating implementation class %s for interface %s",
                            classSpec.name,
                            interfaceEl.getQualifiedName()
                        )
                    );

                    try {
                        JavaFile.builder(GENERATION_PACKAGE, classSpec).build().writeTo(filer);
                    } catch (IOException e) {
                        messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format("Cannot implement Interface %s because of %s.",
                                interfaceEl.getQualifiedName(), e.getMessage()));
                    }
                }
            }
        }

        return true;
    }

    /**
     * 生成实现类的定义.
     *
     * @param interfaceEl 目标接口信息.
     * @return 实现类定义.
     */
    private TypeSpec generateImpl(TypeElement interfaceEl) {
        // 得到实现目标接口需要实现的所有方法.
        List<MethodSpec> methodSpecs =
            collectInterfaceMethods(interfaceEl)
                .stream()
                .map(methodElement -> generateMethodImpl(interfaceEl, methodElement))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        AnnotationDefinition definition = AnnotationHelper.parseClassAnnotation(interfaceEl)
            .stream()
            .filter(d -> d.getFqn().equals(RestfulClient.class.getCanonicalName()))
            .findFirst()
            // 一定会有这个注释,否则整个机制都不会有效.
            .get();
        Optional<?> value = definition.getValue(AnnotationHelper.ANNOTATION_DEFAULT_FIELD_NAME);
        boolean discover = Discovery.isDiscover(value.get().toString());

        String implName = buildImplName(interfaceEl);
        TypeSpec.Builder builder = TypeSpec.classBuilder(implName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(interfaceEl.asType()))
            .addSuperinterface(TypeName.get(Generation.class))
            .addField(generateOkHttpClientField())
            .addMethods(methodSpecs)
            .addJavadoc(buildClassJavaDoc());

        // RestfulClient 注解的值必须会有值.
        if (discover) {
            builder
                .addMethod(generatedConstructor(interfaceEl)) // 无参构建函数.
                .addField(generateDiscoverField())
                .addField(generateLoadBalancerField());
        }

        return builder.build();
    }

    private CodeBlock buildClassJavaDoc() {
        LocalDateTime generationTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return CodeBlock.builder()
            .add("<p>This code is generated by $L.</p>\n", "rpc-restful-standard")
            .add("<p>Generator version: $L</p>\n", this.version)
            .add("<p>Minimum JDK version required: $L</p>\n", this.jdkVersion)
            .add("<p>Generation time: $L</p>\n", generationTime.format(formatter))
            .add("\n")
            .add("<p><strong>Warning:</strong> Do not modify this code manually.</p>\n")
            .build();
    }

    // 无参构造函数.
    private MethodSpec generatedConstructor(TypeElement interfaceEl) {
        String loadBalancerName = parseLoadBalancerName(interfaceEl);

        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            // 初始化名为 LOAD_BALANCER_VARIABLE_NAME 的loadBalancer成员变量.
            .addCode(
                CodeBlock.builder()
                    // loadBalancer = LoadBalancerFactory.getLoadBalancer(loadBalancerName)
                    .addStatement("$L = $T.getLoadBalancer($S)",
                        LOAD_BALANCER_VARIABLE_NAME, LoadBalancerFactory.class, loadBalancerName)
                    .build()
            )
            .build();
    }

    private String parseLoadBalancerName(TypeElement interfaceEl) {
        List<AnnotationDefinition> ads = AnnotationHelper.parseClassAnnotation(interfaceEl);
        String loadBalancerName = ads.stream()
            .filter(ad -> com.vmsmia.framework.component.rpc.restful.annotation.LoadBalancer.class.getName()
                .equals(ad.getFqn()))
            .map(ad -> ad.getValue("value").orElse(null))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .findFirst()
            .orElse(null);

        if (loadBalancerName == null) {
            return LoadBalancers.LEAST_REQUEST;
        } else {
            return loadBalancerName;
        }
    }

    /*
    生成如下成员变量用以处理服务发现.
    <code>
      @Resource
      private Discovery discovery;
    </code>
     */
    private FieldSpec generateDiscoverField() {
        return FieldSpec.builder(Discovery.class, DISCOVER_MEMBER_VARIABLE_NAME)
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(Resource.class)
            .build();
    }

    /*
    生成如下成员变量用以处理okhttpclient.
    <code>
      @Resource
      private OkHttpClient okHttpClient;
    </code>
     */
    private FieldSpec generateOkHttpClientField() {
        return FieldSpec.builder(OkHttpClient.class, OKHTTPCLIENT_MEMBER_VARIABLE_NAME)
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(Resource.class)
            .build();
    }

    private FieldSpec generateLoadBalancerField() {
        return FieldSpec.builder(LoadBalancer.class, LOAD_BALANCER_VARIABLE_NAME)
            .addModifiers(Modifier.PRIVATE)
            .build();
    }

    private String buildImplName(TypeElement interfaceEl) {
        Name interfaceName = interfaceEl.getSimpleName();
        if (configuration.isUseFixedClassName()) {
            // {接口类名}Impl,假如接口名为 TestInterface,则实现类名为 TestInterfaceImpl
            return String.format("%sImpl", interfaceName.toString());
        } else {
            PackageElement packageElement = elements.getPackageOf(interfaceEl);
            String packageName = packageElement.getQualifiedName().toString().replaceAll("\\.", "");
            // {接口类名}Impl_{接口包名去除"."组成的字串},
            // 假如接口名为 com.vmsmai.TestInterface,则实现类名为 TestInterfaceImpl_comvmsmai
            return String.format(
                "%sImpl_%s",
                interfaceName.toString(),
                packageName);
        }
    }

    // 得到按照方法签名排重的方法.
    private Set<ExecutableElement> collectInterfaceMethods(TypeElement interfaceEl) {
        final Set<ExecutableElement> methodElements = new HashSet<>();

        List<TypeMirror> stack = new ArrayList<>();
        List<TypeMirror> currentCopy = new ArrayList<>();
        stack.add(interfaceEl.asType());

        while (!stack.isEmpty()) {
            currentCopy.clear();
            currentCopy.addAll(stack);
            stack.clear();

            currentCopy.stream()
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(tm -> types.asElement(tm))
                .filter(Objects::nonNull)
                .filter(el -> el.getKind() == ElementKind.INTERFACE)
                .map(el -> (TypeElement) el)
                .forEach(ifaceEl -> {
                    // 当前接口的方法.
                    collectInterfaceSelfMethods(ifaceEl, methodElements);

                    // 处理继承的接口.
                    stack.addAll(ifaceEl.getInterfaces());
                });
        }
        return methodElements;
    }

    /*
    收集指定接口的所有需要实现的方法.
    注意: 忽略default标记的方法.
     */
    private void collectInterfaceSelfMethods(TypeElement interfaceEl, Set<ExecutableElement> methodElements) {
        interfaceEl.getEnclosedElements().stream()
            .filter(e -> ElementKind.METHOD == e.getKind())
            .map(e -> (ExecutableElement) e)
            .filter(e -> !e.isDefault())
            .forEach(methodElements::add);
    }

    // 生成实际方法定义,有可能无法生成.
    private MethodSpec generateMethodImpl(TypeElement classElement, ExecutableElement enclosingElement) {
        MethodGenerationStrategy strategy = MethodGenerationStrategyFactory.getStrategy(enclosingElement);
        return strategy.generate(classElement, enclosingElement, processingEnv);
    }

    /**
     * 处理器配置器.
     */
    public static class Configuration {

        /**
         * 值可选true和false,不设置默认为false.<br>
         * true: 生成的类名称为 {目标接口simpleName}Impl.<br>
         * false: 生成的类名称为 {目标接口simpleName}Impl_{uuid}.<br>
         * 区别是是否带有随机UUID,true模式建议一般用以在测试环境需要有可预测的类名时使用.
         */
        public static final String OPTIONS_USE_FIXED_CLASS_NAME = "useFixedClassName";

        private final Map<String, String> options;

        public Configuration(Map<String, String> options) {
            this.options = options;
        }

        /**
         * 是否使用固定类名.
         */
        public boolean isUseFixedClassName() {
            String useFixedClassName = options.get(OPTIONS_USE_FIXED_CLASS_NAME);
            if (useFixedClassName == null) {
                return false;
            } else {
                return Boolean.parseBoolean(useFixedClassName);
            }
        }
    }

    private String readVersion() {
        Properties properties = new Properties();
        try {
            try (InputStream input = getClass().getResourceAsStream("/version.properties")) {
                properties.load(input);
            }
            return properties.getProperty("version");
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.WARNING, "Can not read version.");
            return "UNKNOWN";
        }
    }

    private String readJdkVersion() {
        return System.getProperty("java.version");
    }
}
