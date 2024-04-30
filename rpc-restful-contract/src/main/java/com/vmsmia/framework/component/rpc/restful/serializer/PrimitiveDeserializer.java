package com.vmsmia.framework.component.rpc.restful.serializer;

import com.vmsmia.framework.component.rpc.restful.Primitives;

/**
 * 针对基础类型的解码器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/18 11:31
 * @since 1.8
 */
public class PrimitiveDeserializer implements BytesDeserializer {

    private static final PrimitiveDeserializer INSTANCE = new PrimitiveDeserializer();

    public static PrimitiveDeserializer getInstance() {
        return INSTANCE;
    }

    private PrimitiveDeserializer() {
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> expectType, Object... attachments) {
        if (!Primitives.isPrimitiveOrWrapper(expectType)) {
            throw new IllegalArgumentException("Unsupported type for this deserializer");
        }

        String dataStr = new String(data);
        Object value = null;

        if (expectType == Integer.class || expectType == int.class) {
            value = Integer.parseInt(dataStr);
        } else if (expectType == Long.class || expectType == long.class) {
            value = Long.parseLong(dataStr);
        } else if (expectType == Byte.class || expectType == byte.class) {
            value = Byte.parseByte(dataStr);
        } else if (expectType == Short.class || expectType == short.class) {
            value = Short.parseShort(dataStr);
        } else if (expectType == Character.class || expectType == char.class) {
            value = dataStr.charAt(0);
        } else if (expectType == Boolean.class || expectType == boolean.class) {
            value = Boolean.parseBoolean(dataStr);
        } else if (expectType == Double.class || expectType == double.class) {
            value = Double.parseDouble(dataStr);
        } else if (expectType == Float.class || expectType == float.class) {
            value = Float.parseFloat(dataStr);
        } else {
            throw new SerializationException("Unrecognized primitive type: " + expectType);
        }

        @SuppressWarnings("unchecked")
        T castedValue = (T) value;

        return castedValue;
    }
}
