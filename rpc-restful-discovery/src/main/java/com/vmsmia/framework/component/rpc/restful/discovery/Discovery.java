package com.vmsmia.framework.component.rpc.restful.discovery;

import java.util.List;

/**
 * 服务发现.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 17:05
 * @since 1.8
 */
public interface Discovery {

    String DISCOVER_PREFIX = "discover://";

    /**
     * 获取服务端点.<br>
     * 注意: 返回的端点顺序必须是稳定的,即两次响应如果端点没有发生变化那么顺序是固定的.
     *
     * @param serviceName 服务名称
     * @return 端点
     */
    List<Endpoint> discover(String serviceName);

    /**
     * 判断是否是服务发现.
     */
    static boolean isDiscover(String definition) {
        return definition.toLowerCase().startsWith(DISCOVER_PREFIX);
    }

    /**
     * 解析服务发现名称.
     */
    static String parseDiscoverName(String definition) {
        return definition.substring(DISCOVER_PREFIX.length());
    }

}
