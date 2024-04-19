package com.vmsmia.framework.component.rpc.restful.serializer.bytes;

import com.caucho.hessian.io.Hessian2Output;
import com.vmsmia.framework.component.rpc.restful.serializer.BytesSerializer;
import com.vmsmia.framework.component.rpc.restful.serializer.SerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 11:24
 * @since 1.8
 */
public class HessianBytesSerializer implements BytesSerializer {

    private static final HessianBytesSerializer INSTANCE = new HessianBytesSerializer();

    public static HessianBytesSerializer getInstance() {
        return INSTANCE;
    }

    private HessianBytesSerializer() {
    }

    @Override
    public byte[] serialize(Object source) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(os);
        //这里调用方法的原因是hessian内部没有为此设定一个默认値,每个实例都会创建SerializerFactory.
        //创建SerializerFactory用时很长.
        out.setSerializerFactory(HessianFactoryHolder.SERIALIZER_FACTORY);
        try {
            out.writeObject(source);
            out.close();
        } catch (IOException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }

        return os.toByteArray();
    }
}
