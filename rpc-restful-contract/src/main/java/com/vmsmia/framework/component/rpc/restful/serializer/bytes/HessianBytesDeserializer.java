package com.vmsmia.framework.component.rpc.restful.serializer.bytes;

import com.caucho.hessian.io.Hessian2Input;
import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.SerializationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 13:36
 * @since 1.8
 */
public class HessianBytesDeserializer implements BytesDeserializer {

    private static final HessianBytesDeserializer INSTANCE = new HessianBytesDeserializer();

    public static HessianBytesDeserializer getInstance() {
        return INSTANCE;
    }
    
    private HessianBytesDeserializer() {
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> expectType, Object... unused) {
        ByteArrayInputStream os = new ByteArrayInputStream(data);
        Hessian2Input input = new Hessian2Input(os);
        input.setSerializerFactory(HessianFactoryHolder.SERIALIZER_FACTORY);
        Object obj = null;
        try {
            obj = input.readObject();
        } catch (IOException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }

        return expectType.cast(obj);
    }
}
