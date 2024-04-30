package com.vmsmia.framework.component.rpc.restful.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 来源数据是字会串的byte数组类型的反序列化器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/18 11:24
 * @since 1.8
 */
public abstract class AbstractStringDeserializer implements BytesDeserializer {
    @Override
    public <T> T deserialize(byte[] data, Class<T> expectType, Object... attachments) {
        Charset charset = getCharset(attachments);
        return doDeserialize(data, expectType, charset);
    }

    protected abstract <T> T doDeserialize(byte[] data, Class<T> expectType, Charset charset);

    private Charset getCharset(Object... attachments) {
        if (attachments != null) {
            for (Object attachment : attachments) {
                if (attachment instanceof Charset) {
                    return (Charset) attachment;
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
