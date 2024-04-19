package com.vmsmia.framework.component.rpc.restful.serializer.bytes;

import com.caucho.hessian.io.SerializerFactory;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 14:06
 * @since 1.8
 */
public class HessianFactoryHolder {
    public static final SerializerFactory SERIALIZER_FACTORY = new SerializerFactory();
}
