package com.vmsmia.framework.component.rpc.restful.serializer.string;

import com.vmsmia.framework.component.rpc.restful.serializer.AbstractStringDeserializer;
import java.nio.charset.Charset;

/**
 * 解码成字符串的解码器, 可以通过附件指定Charset.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 15:51
 * @since 1.8
 */
public class PlainStringDeserializer extends AbstractStringDeserializer {

    private static final PlainStringDeserializer INSTANCE = new PlainStringDeserializer();

    public static PlainStringDeserializer getInstance() {
        return INSTANCE;
    }

    private PlainStringDeserializer() {
    }

    @Override
    protected <T> T doDeserialize(byte[] data, Class<T> expectType, Charset charset) {
        if (!expectType.equals(String.class)) {
            throw new IllegalArgumentException("PlainStringDeserializer only supports deserialization to String.");
        } else {
            return expectType.cast(new String(data, charset));
        }
    }
}
