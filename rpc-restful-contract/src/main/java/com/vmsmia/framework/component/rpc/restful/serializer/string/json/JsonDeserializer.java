package com.vmsmia.framework.component.rpc.restful.serializer.string.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vmsmia.framework.component.rpc.restful.serializer.AbstractStringDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.SerializationException;
import java.nio.charset.Charset;

/**
 * json 解码器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/11 13:18
 * @since 1.8
 */
public class JsonDeserializer extends AbstractStringDeserializer {

    private static final JsonDeserializer INSTANCE = new JsonDeserializer();

    public static JsonDeserializer getInstance() {
        return INSTANCE;
    }

    private JsonDeserializer() {
    }

    @Override
    protected <T> T doDeserialize(byte[] data, Class<T> expectType, Charset charset) {
        try {
            return Json.deserialize(new String(data, charset), expectType);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
