package com.vmsmia.framework.component.rpc.restful.standard;

import java.io.IOException;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * {@code GenerationClassSpringRegistry} 是用于自动注册指定包下继承了 {@code Generation} 接口的类为 Spring 容器管理的 Bean 的类.
 * 该类实现了 Spring 的 {@link BeanDefinitionRegistryPostProcessor} 接口，使其能在 Spring 容器加载 Bean 定义之后、初始化 Bean 之前.
 * 修改或添加 Bean 定义。
 *
 * <p>该类的主要作用是扫描指定包（{@link RpcClientProcessor#GENERATION_PACKAGE}）下所有的类文件，
 * 检查这些类是否实现了 {@code Generation} 接口，
 * 如果实现了，则自动将这些类注册到 Spring 容器中，由 Spring 管理其生命周期。</p>
 *
 * <p>在 {@code postProcessBeanDefinitionRegistry} 方法中，
 * 通过 Spring 的 {@link PathMatchingResourcePatternResolver} 来实现对指定包下类文件的扫描，
 * 并通过反射机制检查类是否实现了 {@code Generation} 接口，满足条件的类会被注册到 Spring 容器中。</p>
 *
 * <p>请注意，在使用该类时需要确保 {@code RpcClientProcessor.GENERATION_PACKAGE} 已经正确设置为目标包的路径，
 * 并且目标包下的类已经正确地实现了 {@code Generation} 接口。</p>
 *
 * <p>{@code postProcessBeanFactory} 方法在本类中未实现具体的逻辑，留给子类根据需要进行实现。</p>
 *
 * <p>使用该类可以方便地实现在 Spring 应用中自动注册特定功能类的需求，提高了代码的灵活性和可扩展性。</p>
 *
 * @author bin.dong
 * @version 0.1 2024/4/30 17:18
 * @see BeanDefinitionRegistryPostProcessor
 * @see RpcClientProcessor#GENERATION_PACKAGE
 * @see Generation
 * @since 1.8
 */
public class GenerationClassSpringRegistry implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String scanPattern = RpcClientProcessor.GENERATION_PACKAGE.replace('.', '/') + "/*.class";

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(scanPattern);
            for (Resource resource : resources) {
                String resourceName = resource.getFilename();
                if (resourceName == null) {
                    continue;
                }
                String className = resourceName.replace(".class", "");
                Class<?> clazz = Class.forName(RpcClientProcessor.GENERATION_PACKAGE + "." + className);
                if (clazz.isAssignableFrom(Generation.class)) {
                    BeanDefinition beanDefinition =
                        BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition();
                    registry.registerBeanDefinition(className, beanDefinition);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new FatalBeanException(e.getMessage(), e);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing.
    }
}

