package com.vmsmia.framework.component.rpc.restful.serializer;

/**
 * @author bin.dong
 * @version 0.1 2024/4/11 10:25
 * @since 1.8
 */
public interface Serializer<R> {
    R serialize(Object obj);
}
