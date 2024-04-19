package com.vmsmia.framework.component.rpc.restful.standard.client.discovery;

/**
 * @author bin.dong
 * @version 0.1 2024/4/17 18:04
 * @since 1.8
 */
public class DiscoveryHelper {

    private static final String DISCOVER_PREFIX = "discover://";

    private DiscoveryHelper() {
    }

    public static boolean isDiscover(String definition) {
        return definition.toLowerCase().startsWith(DISCOVER_PREFIX);
    }

    public static String parseDiscoverName(String definition) {
        return definition.substring(DISCOVER_PREFIX.length());
    }
}
