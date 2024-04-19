package com.vmsmia.framework.component.rpc.restful.standard.client.discovery;

import java.util.Objects;

/**
 * @author bin.dong
 * @version 0.1 2024/4/17 17:05
 * @since 1.8
 */
public class Endpoint {

    private String host;
    private int port;
    private boolean tls;

    public Endpoint(String host) {
        this(host, 80, false);
    }

    public Endpoint(String host, int port) {
        this(host, port, false);
    }

    public Endpoint(String host, int port, boolean tls) {
        this.host = host;
        this.port = port;
        this.tls = tls;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isTls() {
        return tls;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(host, port, tls);
    }
}
