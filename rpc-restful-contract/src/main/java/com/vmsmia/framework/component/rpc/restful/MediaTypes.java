package com.vmsmia.framework.component.rpc.restful;

import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.PrimitiveDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.bytes.PlainBytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.string.PlainStringDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.string.json.JsonDeserializer;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 媒体类型的工具.
 *
 * @author bin.dong
 * @version 0.1 2024/4/11 11:37
 * @since 1.8
 */
public final class MediaTypes {

    /**
     * 默认的媒体类型.
     */
    public static final MediaType DEFAULT_MEDIA_TYPE = MediaType.create("text/plain; charset=UTF-8");

    private MediaTypes() {
    }

    /**
     * 获取智能选择的响应解析器.
     *
     * @param mediaType 媒体类型.
     * @return 响应解析器.
     */
    public static Optional<BytesDeserializer> getSmartDeserializer(MediaType mediaType, Class<?> returnType) {
        if (mediaType.isJson()) {

            if (returnType == String.class) {
                return Optional.ofNullable(PlainStringDeserializer.getInstance());
            } else if (returnType == byte[].class) {
                // 原始字节返回.
                return Optional.ofNullable(PlainBytesDeserializer.getInstance());
            } else {
                return Optional.ofNullable(JsonDeserializer.getInstance());
            }

        } else if (mediaType.isText()) {

            if (returnType == String.class) {
                return Optional.ofNullable(PlainStringDeserializer.getInstance());
            } else if (returnType == byte[].class) {
                return Optional.ofNullable(PlainBytesDeserializer.getInstance());
            } else if (Primitives.isPrimitiveOrWrapper(returnType)) {
                return Optional.ofNullable(PrimitiveDeserializer.getInstance());
            } else {
                return Optional.empty();
            }

        } else if (mediaType.isXml()) {

            return Optional.ofNullable(PlainStringDeserializer.getInstance());

        } else if (mediaType.isBinary()) {
            if (returnType == byte[].class) {
                return Optional.ofNullable(PlainBytesDeserializer.getInstance());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}

