package com.vmsmia.framework.component.rpc.restful.serializer.string.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * JSON的帮助工具.
 *
 * @author bin.dong
 * @version 0.1 2024/4/11 13:19
 * @since 1.8
 */
public class Json {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        .enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonReadFeature.ALLOW_YAML_COMMENTS).build();

    /**
     * 序列化.
     *
     * @param obj 目标对象.
     * @return 序列化的JSON字符串.
     * @throws JsonProcessingException 序列化失败.
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    /**
     * 反序列化.
     *
     * @param json       目标json字符串.
     * @param expectType 预期类型.
     * @param <T>        预期类型.
     * @return 反序列化后的对象.
     * @throws JsonProcessingException 反序列化失败.
     */
    public static <T> T deserialize(String json, Class<T> expectType) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, expectType);
    }
}
