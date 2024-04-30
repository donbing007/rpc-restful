package com.vmsmia.framework.component.rpc.restful.discovery;

import java.util.Collections;
import java.util.List;

/**
 * @author bin.dong
 * @version 0.1 2024/4/22 17:38
 * @since 1.8
 */
public class FileDiscover implements Discovery {
    @Override
    public List<Endpoint> discover(String serviceName) {
        return Collections.emptyList();
    }
}
