package com.vmsmia.framework.component.rpc.restful.standard.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vmsmia.framework.component.rpc.restful.serializer.string.json.Json;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author bin.dong
 * @version 0.1 2024/4/16 19:32
 * @since 1.8
 */
public class HttpClientTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    public void testGet() throws Exception {
        Data expectedData = new Data("test", 1);

        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(expectedData))
            .addHeader("Content-Type", "application/json; charset=utf8"));


        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data/{name}")
            .withPathVariables(Collections.singletonMap("name", "test"))
            .withQueryParams(Collections.singletonMap("id", "123"))
            .withHeaders(Collections.singletonList(new AbstractMap.SimpleEntry<>("visionmc", "test")))
            .build();
        Data result = client.get(Data.class);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedData, result);
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/data/test?id=123", recordedRequest.getPath());
        assertEquals(0, recordedRequest.getBodySize());
        assertEquals("test", recordedRequest.getHeader("visionmc"));
    }

    @Test
    public void testPost() throws Exception {
        Data returnData = new Data("return", 1);

        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(returnData))
            .addHeader("Content-Type", "application/json; charset=utf8"));

        Data submitData = new Data("submit", 10);
        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data/{name}")
            .withPathVariables(Collections.singletonMap("name", "test"))
            .withQueryParams(Collections.singletonMap("id", "123"))
            .withHeaders(Collections.singletonList(new AbstractMap.SimpleEntry<>("visionmc", "test")))
            .withBody(Json.serialize(submitData).getBytes(StandardCharsets.UTF_8))
            .withBodyMediaType("application/json; charset=utf8")
            .build();
        Data resultData = client.post(Data.class);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(returnData, resultData);
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/data/test?id=123", recordedRequest.getPath());
        assertEquals("test", recordedRequest.getHeader("visionmc"));
        assertEquals(submitData,
            Json.deserialize(new String(recordedRequest.getBody().readByteArray(), StandardCharsets.UTF_8), Data.class));
    }

    @Test
    public void testHead() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .addHeader("Custom-Header", "value"));

        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data")
            .withHeaders(Collections.singletonList(new AbstractMap.SimpleEntry<>("visionmc", "test")))
            .build();
        List<Map.Entry<String, String>> headers = client.head();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("HEAD", recordedRequest.getMethod());
        assertEquals("/data", recordedRequest.getPath());
        assertTrue(headers.stream().anyMatch(e -> "Custom-Header".equals(e.getKey()) && "value".equals(e.getValue())));
    }

    @Test
    public void testPatch() throws Exception {
        Data returnData = new Data("return", 2);

        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(returnData))
            .addHeader("Content-Type", "application/json; charset=utf8"));

        Data submitData = new Data("submit", 20);
        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data")
            .withBody(Json.serialize(submitData).getBytes(StandardCharsets.UTF_8))
            .withBodyMediaType("application/json; charset=utf8")
            .withHeaders(Collections.singletonList(new AbstractMap.SimpleEntry<>("visionmc", "test")))
            .build();
        Data resultData = client.patch(Data.class);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(returnData, resultData);
        assertEquals("PATCH", recordedRequest.getMethod());
        assertEquals("/data", recordedRequest.getPath());
        assertEquals("test", recordedRequest.getHeader("visionmc"));
        assertEquals(submitData,
            Json.deserialize(new String(recordedRequest.getBody().readByteArray(), StandardCharsets.UTF_8), Data.class));
    }

    @Test
    public void testPut() throws Exception {
        Data returnData = new Data("return", 3);

        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(returnData))
            .addHeader("Content-Type", "application/json; charset=utf8"));

        Data submitData = new Data("submit", 30);
        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data")
            .withHeaders(Collections.singletonList(new AbstractMap.SimpleEntry<>("visionmc", "test")))
            .withBody(Json.serialize(submitData).getBytes(StandardCharsets.UTF_8))
            .withBodyMediaType("application/json; charset=utf8")
            .build();
        Data resultData = client.put(Data.class);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(returnData, resultData);
        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals("/data", recordedRequest.getPath());
        assertEquals("test", recordedRequest.getHeader("visionmc"));
        assertEquals(submitData,
            Json.deserialize(new String(recordedRequest.getBody().readByteArray(), StandardCharsets.UTF_8), Data.class));
    }

    @Test
    public void testDelete() throws Exception {
        Data expectedData = new Data("delete", 4);

        mockWebServer.enqueue(new MockResponse()
            .setBody(Json.serialize(expectedData))
            .addHeader("Content-Type", "application/json; charset=utf8"));

        HttpClient client = HttpClient.Builder.anBuilder()
            .withBaseUrl("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort())
            .withPathTemplate("/data/{id}")
            .withPathVariables(Collections.singletonMap("id", "4"))
            .build();
        Data result = client.delete(Data.class);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals("/data/4", recordedRequest.getPath());
        assertEquals(expectedData, result);
    }

    private static class Data {
        private String c1;
        private int c2;

        public Data() {
        }

        public Data(String c1, int c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        public String getC1() {
            return c1;
        }

        public void setC1(String c1) {
            this.c1 = c1;
        }

        public int getC2() {
            return c2;
        }

        public void setC2(int c2) {
            this.c2 = c2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Data data = (Data) o;
            return c2 == data.c2 && Objects.equals(c1, data.c1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(c1, c2);
        }
    }
}