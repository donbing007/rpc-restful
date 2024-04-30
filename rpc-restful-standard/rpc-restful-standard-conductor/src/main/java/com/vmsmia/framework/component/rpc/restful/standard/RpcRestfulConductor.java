package com.vmsmia.framework.component.rpc.restful.standard;

import com.vmsmia.framework.component.rpc.restful.discovery.Discovery;
import com.vmsmia.framework.component.rpc.restful.discovery.KubernetesServiceDiscover;
import com.vmsmia.framework.component.rpc.restful.standard.config.DiscoveryConfig;
import com.vmsmia.framework.component.rpc.restful.standard.config.RpcRestfulConfig;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 目标是如下的配置.
 * <pre>
 *     vmsmia:
 *       component:
 *         rpc-restful:
 *           provider: standard
 *           config:
 *             writeTimeoutMs: 10000
 *             readTimeoutMs: 10000
 *             connectTimeoutMs: 10000
 *             threadPoolSize: 10
 *             maxRequest: 200
 *             discovery:
 *               provider: (kubernetes | static)
 *               kubernetes:
 *                 allNamespace: true
 *                 effectiveTimeMs: 10000
 *               static:
 *                 url: (http:// | file://)
 *             load-balancer:
 *               provider: (random | round-robin | least-active)
 * </pre>
 *
 * @author bin.dong
 * @version 0.1 2024/4/30 17:09
 * @since 1.8
 */
@Configuration
@EnableConfigurationProperties(RpcRestfulConfig.class)
@ComponentScan(basePackages = "com.vmsmia.framework.component.rpc-restful.standard")
@ConditionalOnProperty(
    prefix = "vmsmia.component.rpc-restful",
    value = "provider",
    havingValue = "standard",
    matchIfMissing = false)
public class RpcRestfulConductor {

    private ExecutorService works;

    @Resource
    private RpcRestfulConfig config;

    @Bean
    @DependsOn({"okhttpClient", "discovery"})
    public GenerationClassSpringRegistry generationClassSpringRegistry() {
        return new GenerationClassSpringRegistry();
    }

    @Bean
    public ExecutorService okhttp3ExecutorService() {
        works = new ThreadPoolExecutor(
            config.getThreadPoolSize(),
            config.getThreadPoolSize(),
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(200),
            r -> {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("okhttp3-" + System.currentTimeMillis());
                return thread;
            }
        );
        return works;
    }

    @Bean(name = "restfulStandardOkHttp3Client")
    public OkHttpClient okHttpClient() {
        Dispatcher dispatcher = new Dispatcher(works);
        dispatcher.setMaxRequests(config.getMaxRequest());
        dispatcher.setMaxRequestsPerHost(config.getMaxRequest());

        return new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
            .dispatcher(dispatcher)
            .build();
    }

    @Bean
    public Discovery discovery() {
        DiscoveryConfig discoveryConfig = config.getDiscovery();
        if (discoveryConfig == null) {
            throw new IllegalArgumentException("discovery config is null");
        }
        switch (discoveryConfig.getProvider()) {
            case DiscoveryConfig.KUBERNETES_PROVIDER: {
                return new KubernetesServiceDiscover(
                    discoveryConfig.getKubernetes().getAllNamespace(),
                    discoveryConfig.getKubernetes().getEffectiveTimeMs()
                );
            }
            case DiscoveryConfig.STATIC_PROVIDER: {
                // TODO: 静态服务定义.
                return null;
            }
            default:
                throw new IllegalArgumentException("discovery provider is not support");
        }


    }

    @PreDestroy
    public void destroy() {
        if (works != null) {
            works.shutdown();
            try {
                if (!works.awaitTermination(60, TimeUnit.SECONDS)) {
                    works.shutdownNow();
                    if (!works.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("OkHttp3 not fail shutdown！");
                    }
                }
            } catch (InterruptedException ie) {
                works.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
