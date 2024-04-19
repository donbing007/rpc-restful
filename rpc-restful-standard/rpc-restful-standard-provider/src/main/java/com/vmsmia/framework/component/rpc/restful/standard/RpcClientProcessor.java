package com.vmsmia.framework.component.rpc.restful.standard;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.vmsmia.framework.component.rpc.restful.standard.client.discovery.Discovery;
import com.vmsmia.framework.component.rpc.restful.standard.generation.MethodGenerationStrategy;
import com.vmsmia.framework.component.rpc.restful.standard.generation.MethodGenerationStrategyFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
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
     * 服务发现成员变量名称.
     */
    public static final String DISCOVER_MEMBER_VARIABLE_NAME = "discovery";
    private Filer filer;
    private Messager messager;
    private Types types;
    private Configuration configuration;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.types = processingEnv.getTypeUtils();
        this.configuration = new Configuration(processingEnv.getOptions());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> els = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element el : els) {
                if (el.getKind() == ElementKind.INTERFACE) {
                    TypeElement interfaceEl = (TypeElement) el;
                    TypeSpec classSpec = generateImpl(interfaceEl);

                    System.out.println(classSpec.toString());

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
                .collect(Collectors.toList());

        Name name = interfaceEl.getSimpleName();
        String implName = buildImplName(name);
        return TypeSpec.classBuilder(implName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(interfaceEl.asType()))
            .addField(generateDiscoverField())
            .addMethods(methodSpecs)
            .build();
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

    private String buildImplName(Name interfaceName) {
        if (configuration.isUseFixedClassName()) {
            // {接口类名}Impl_{UUID},假如接口名为 TestInterface,则实现类名为 TestInterfaceImpl
            return String.format("%sImpl", interfaceName.toString());
        } else {
            // {接口类名}Impl_{UUID},假如接口名为 TestInterface,则实现类名为 TestInterfaceImpl_4d9358eefe8a4b1985b565b667d5c2eb
            return String.format(
                "%sImpl_%s",
                interfaceName.toString(),
                UUID.randomUUID().toString().replaceAll("-", ""));
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
}
