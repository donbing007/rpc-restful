package com.vmsmia.framework.component.rpc.restful.serializer.string.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vmsmia.framework.component.rpc.restful.serializer.SerializationException;
import com.vmsmia.framework.component.rpc.restful.serializer.StringSerializer;

/**
 * 编码结果是JSON字符串.
 *
 * @author bin.dong
 * @version 0.1 2024/4/11 11:08
 * @since 1.8
 */
public class JsonSerializer implements StringSerializer {

    private static final JsonSerializer INSTANCE = new JsonSerializer();

    public static JsonSerializer getInstance() {
        return INSTANCE;
    }

    private JsonSerializer() {
    }

    @Override
    public String serialize(Object obj) {
        try {
            return Json.serialize(obj);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
