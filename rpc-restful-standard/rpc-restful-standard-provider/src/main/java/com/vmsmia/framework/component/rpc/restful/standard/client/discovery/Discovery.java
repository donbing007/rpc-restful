package com.vmsmia.framework.component.rpc.restful.standard.client.discovery;

import java.util.Optional;

/**
 * 服务发现.
 *
 * @author bin.dong
 * @version 0.1 2024/4/17 17:05
 * @since 1.8
 */
public interface Discovery {

    /**
     * 获取服务端点.
     *
     * @param serviceName 服务名称
     * @return 端点
     */
    Optional<Endpoint> discover(String serviceName);

}
