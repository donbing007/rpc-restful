package com.vmsmia.framework.component.rpc.restful.serializer;

/**
 * 反序列化接口定义.
 *
 * @author bin.dong
 * @version 0.1 2024/4/11 11:23
 * @since 1.8
 */
public interface Deserializer<S> {

    <T> T deserialize(S data, Class<T> expectType, Object... attachments);

}
