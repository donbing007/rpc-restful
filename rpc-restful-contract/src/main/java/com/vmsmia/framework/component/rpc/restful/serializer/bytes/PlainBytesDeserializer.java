package com.vmsmia.framework.component.rpc.restful.serializer.bytes;

import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;

/**
 * 二进制解码器.预期数据一定是一个二进制数组.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 16:04
 * @since 1.8
 */
public class PlainBytesDeserializer implements BytesDeserializer {

    private static final PlainBytesDeserializer INSTANCE = new PlainBytesDeserializer();

    public static PlainBytesDeserializer getInstance() {
        return INSTANCE;
    }

    private PlainBytesDeserializer() {

    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> expectType, Object... unused) {
        if (!expectType.equals(byte[].class)) {
            throw new IllegalArgumentException("PlainBytesDeserializer only supports deserialization to byte[].");
        } else {
            return expectType.cast(data);
        }
    }
}
