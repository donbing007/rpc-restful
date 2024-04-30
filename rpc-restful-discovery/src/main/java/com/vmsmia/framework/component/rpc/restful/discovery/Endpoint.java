package com.vmsmia.framework.component.rpc.restful.discovery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * 表示一个网络端点的类，可以指定主机、端口和是否使用TLS.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 17:05
 * @since 1.8
 */
public class Endpoint implements Comparable<Endpoint> {

    private static final Endpoint DEFAULT_ENDPOINT = new Endpoint("localhost", 80, false, 0.0F);

    private final String host; // 主机地址
    private final int port; // 端口号
    private final boolean tls; // 是否使用TLS加密通信
    private final float weight; // 权重

    /**
     * 根据一段统一资源表达式创建一个Endpoint对象.
     *
     * @param spec 表达式.
     * @return endpoint实例.
     */
    public static Endpoint parse(String spec) {
        if (spec == null || spec.isEmpty()) {
            throw new IllegalArgumentException("Invalid URL character representation.");
        }
        URL url;
        try {
            url = new URL(spec);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                String.format("Invalid URL character representation.[%s]", e.getMessage()), e);
        }

        boolean tls;
        if (url.getProtocol().equalsIgnoreCase("http")) {
            tls = false;
        } else if (url.getProtocol().equalsIgnoreCase("https")) {
            tls = true;
        } else {
            throw new IllegalArgumentException(String.format("Invalid URL protocol.[%s]", url.getProtocol()));
        }

        int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();

        return new Endpoint(url.getHost(), port, tls);
    }

    /**
     * 默认端点.
     */
    public static Endpoint defaultEndpoint() {
        return DEFAULT_ENDPOINT;
    }

    /**
     * 构造函数，创建一个默认端口为80，不使用TLS的端点.
     *
     * @param host 主机地址。
     */
    public Endpoint(String host) {
        this(host, 80, false, 0.0F);
    }

    /**
     * 构造函数，创建一个指定端口，不使用TLS的端点.
     *
     * @param host 主机地址。
     * @param port 端口号。
     */
    public Endpoint(String host, int port) {
        this(host, port, false, 0.0F);
    }

    /**
     * 构造函数 ,创建一个指定端口, 权重为默认值.
     *
     * @param host 主机地址.
     * @param port 端口号.
     * @param tls  是否使用TLS加密通讯.
     */
    public Endpoint(String host, int port, boolean tls) {
        this(host, port, tls, 0.0F);
    }

    /**
     * 构造函数，创建一个指定主机、端口和是否使用TLS的端点.
     *
     * @param host 主机地址.
     * @param port 端口号.
     * @param tls  是否使用TLS加密通信.
     */
    public Endpoint(String host, int port, boolean tls, float weight) {
        this.host = host;
        this.port = port;
        this.tls = tls;
        this.weight = weight;
    }

    /**
     * 获取主机地址.
     *
     * @return 主机地址字符串.
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取端口号.
     *
     * @return 端口号.
     */
    public int getPort() {
        return port;
    }

    /**
     * 判断该端点是否使用TLS.
     *
     * @return 如果使用TLS返回true，否则返回false.
     */
    public boolean isTls() {
        return tls;
    }

    /**
     * 端点的权重.
     *
     * @return 范围是[0.0, 1.0].
     */
    public float getWeight() {
        return weight;
    }

    /**
     * 比较两个端点是否相等.
     *
     * @param o 另一个端点对象.
     * @return 如果两个端点的主机、端口和TLS设置都相同，则返回true；否则返回false。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) o;
        return port == endpoint.port && tls == endpoint.tls && Objects.equals(host, endpoint.host);
    }

    /**
     * 生成端点的哈希码.
     *
     * @return 端点的哈希码.
     */
    @Override
    public int hashCode() {
        return Objects.hash(host, port, tls);
    }

    /**
     * 比较当前端点和另一个端点的排序值.
     *
     * @param o 另一个端点对象.
     * @return 返回两个端点排序值的比较结果。
     */
    @Override
    public int compareTo(Endpoint o) {
        return Integer.compare(calculateEndpointSortValue(), o.calculateEndpointSortValue());
    }

    @Override
    public String toString() {
        return "Endpoint{" + "host='" + host + '\''
            + ", port=" + port
            + ", tls=" + tls
            + ", weight=" + weight
            + '}';
    }

    private int calculateEndpointSortValue() {
        int hostNumber = host.hashCode();
        int tlsNumber = tls ? 1 : 0;
        return (hostNumber << 17) | ((port & 0xFFFF) << 1) | tlsNumber;
    }
}