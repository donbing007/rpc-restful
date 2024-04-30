package com.vmsmia.framework.component.rpc.restful;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * @author bin.dong
 * @version 0.1 2024/4/18 18:45
 * @since 1.8
 */
public class MediaTypeTest {

    @Test
    void testCreateWithValidContentType() {
        MediaType mediaType = MediaType.create("application/json; charset=UTF-8; profile=user");
        assertEquals("application", mediaType.getType());
        assertEquals("json", mediaType.getSubType());
        assertEquals(2, mediaType.getArgs().size());
        assertTrue(mediaType.getArgs().contains(new AbstractMap.SimpleEntry<>("charset", "UTF-8")));
        assertTrue(mediaType.getArgs().contains(new AbstractMap.SimpleEntry<>("profile", "user")));
    }

    @Test
    void testCreateWithNoArgs() {
        MediaType mediaType = MediaType.create("text/plain");
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubType());
        assertTrue(mediaType.getArgs().isEmpty());
    }

    @Test
    void testCreateWithEmptyString() {
        MediaType mediaType = MediaType.create("");
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubType());
        assertTrue(mediaType.getArgs().size() == 1);
        assertEquals("UTF-8", mediaType.getArgs().get(0).getValue());
    }

    @Test
    void testCreateWithNull() {
        MediaType mediaType = MediaType.create(null);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubType());
        assertTrue(mediaType.getArgs().size() == 1);
        assertEquals("UTF-8", mediaType.getArgs().get(0).getValue());
    }

    @Test
    void testGetType() {
        MediaType mediaType = MediaType.create("application/json");
        assertEquals("application", mediaType.getType());
    }

    @Test
    void testGetSubType() {
        MediaType mediaType = MediaType.create("application/json");
        assertEquals("json", mediaType.getSubType());
    }

    @Test
    void testGetArgs() {
        MediaType mediaType = MediaType.create("application/json; charset=UTF-8");
        List<Map.Entry<String, String>> expectedArgs = Collections.singletonList(
            new AbstractMap.SimpleEntry<>("charset", "UTF-8")
        );
        assertEquals(expectedArgs, mediaType.getArgs());
    }

    @Test
    void testGetArg() {
        MediaType mediaType = MediaType.create("application/json; charset=UTF-8");
        assertEquals(Optional.of("UTF-8"), mediaType.getArg("charset"));
        assertEquals(Optional.empty(), mediaType.getArg("unknown"));
    }

    @Test
    void testIsJson() {
        MediaType jsonMediaType = MediaType.create("application/json");
        assertTrue(jsonMediaType.isJson());

        MediaType nonJsonMediaType = MediaType.create("text/plain");
        assertFalse(nonJsonMediaType.isJson());
    }

    @Test
    void testIsText() {
        MediaType textMediaType = MediaType.create("text/plain");
        assertTrue(textMediaType.isText());

        MediaType nonTextMediaType = MediaType.create("application/json");
        assertFalse(nonTextMediaType.isText());
    }

    @Test
    void testIsXml() {
        MediaType xmlMediaType = MediaType.create("application/xml");
        assertTrue(xmlMediaType.isXml());

        MediaType xmlTextMediaType = MediaType.create("text/xml");
        assertTrue(xmlTextMediaType.isXml());

        MediaType nonXmlMediaType = MediaType.create("application/json");
        assertFalse(nonXmlMediaType.isXml());
    }

    @Test
    void testIsBinary() {
        MediaType binaryMediaType = MediaType.create("image/png");
        assertTrue(binaryMediaType.isBinary());

        MediaType nonBinaryMediaType = MediaType.create("application/json");
        assertFalse(nonBinaryMediaType.isBinary());
    }

    @Test
    void testToString() {
        MediaType mediaType = MediaType.create("application/json; charset=UTF-8; profile=user");
        assertEquals("application/json; charset=UTF-8; profile=user", mediaType.toString());
    }

    @Test
    void testParse() {
        MediaType mediaType = MediaType.create("application/json; charset=UTF-8; profile=user");
        assertEquals("application", mediaType.getType());
        assertEquals("json", mediaType.getSubType());
        assertEquals(2, mediaType.getArgs().size());
        assertEquals("UTF-8", mediaType.getArg("charset").get());
        assertEquals("user", mediaType.getArg("profile").get());
    }
}