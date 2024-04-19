package com.vmsmia.framework.component.rpc.restful;

import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.PrimitiveDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.bytes.PlainBytesDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.string.PlainStringDeserializer;
import com.vmsmia.framework.component.rpc.restful.serializer.string.json.JsonDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author bin.dong
 * @version 0.1 2024/4/18 18:57
 * @since 1.8
 */
public class MediaTypesTest {

    @Test
    public void testGetSmartDeserializerForJsonWithStringReturnType() {
        MediaType mediaType = MediaType.create("application/json");
        Class<?> returnType = String.class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainStringDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForJsonWithByteReturnType() {
        MediaType mediaType = MediaType.create("application/json");
        Class<?> returnType = byte[].class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainBytesDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForJsonWithPrimitiveReturnType() {
        MediaType mediaType = MediaType.create("application/json");
        Class<?> returnType = int.class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof JsonDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForTextWithStringReturnType() {
        MediaType mediaType = MediaType.create("text/plain");
        Class<?> returnType = String.class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainStringDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForTextWithByteReturnType() {
        MediaType mediaType = MediaType.create("text/plain");
        Class<?> returnType = byte[].class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainBytesDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForTextWithPrimitiveReturnType() {
        MediaType mediaType = MediaType.create("text/plain");
        Class<?> returnType = boolean.class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PrimitiveDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForXmlWithStringReturnType() {
        MediaType mediaType = MediaType.create("text/xml");
        Class<?> returnType = String.class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainStringDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForXmlWithByteReturnType() {
        MediaType mediaType = MediaType.create("text/xml");
        Class<?> returnType = Object.class;
        Assertions.assertFalse(MediaTypes.getSmartDeserializer(mediaType, returnType).isPresent());
    }

    @Test
    public void testGetSmartDeserializerForBinaryWithByteReturnType() {
        MediaType mediaType = MediaType.create("application/octet-stream");
        Class<?> returnType = byte[].class;
        BytesDeserializer deserializer = MediaTypes.getSmartDeserializer(mediaType, returnType).get();
        Assertions.assertTrue(deserializer instanceof PlainBytesDeserializer);
    }

    @Test
    public void testGetSmartDeserializerForBinaryWithNonByteReturnType() {
        MediaType mediaType = MediaType.create("application/octet-stream");
        Class<?> returnType = String.class;
        Assertions.assertFalse(MediaTypes.getSmartDeserializer(mediaType, returnType).isPresent());
    }

    @Test
    public void testGetSmartDeserializerForUnsupportedMediaType() {
        MediaType mediaType = MediaType.create("application/pdf");
        Class<?> returnType = String.class;
        Assertions.assertFalse(MediaTypes.getSmartDeserializer(mediaType, returnType).isPresent());
    }

}