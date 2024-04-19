package com.vmsmia.framework.component.rpc.restful.standard.client;

import com.vmsmia.framework.component.rpc.restful.MediaTypes;
import com.vmsmia.framework.component.rpc.restful.serializer.BytesDeserializer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import kotlin.Pair;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 基于OkHttp3的HTTP客户端类.
 * 提供了构建和执行HTTP请求的方法。
 * 支持GET、POST、PUT、DELETE、PATCH和HEAD请求方法。
 *
 * @author bin.dong
 * @version 0.1 2024/4/12 16:43
 * @since 1.8
 */
public class HttpClient {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final String DEFAULT_BODY_MEDIA_TYPE = "application/json charset=utf-8";

    public static final char PATH_VARIABLE_PREFIX = '{';
    public static final char PATH_VARIABLE_SUFFIX = '}';

    private String baseUrl;
    private String pathTemplate;
    private Map<String, String> pathVariables;
    private Map<String, String> queryParams;
    private List<Map.Entry<String, String>> headers;
    private Object body;
    private String bodyMediaType;
    private BytesDeserializer returnDeserializer;
    private long readTimeoutMs = 0;
    private long connectTimeoutMs = 0;
    private long writeTimeoutMs = 0;

    private HttpClient() {
    }

    /**
     * 执行HTTP GET请求.
     *
     * @param expectType 期望返回的对象类型
     * @param <T>        泛型参数，与expectType相对应的类型
     * @return 服务器响应体被解析为expectType指定类型的对象
     * @throws IOException 如果发生I/O错误
     */
    public <T> T get(Class<T> expectType) throws IOException {
        Request req = doBuildRequestBuilder().get().build();

        try (Response res = call(req)) {
            return parseBody(res, expectType);
        }
    }

    /**
     * 执行HTTP DELETE请求.
     *
     * @param expectType 期望返回的对象类型
     * @param <T>        泛型参数，与expectType相对应的类型
     * @return 服务器响应体被解析为expectType指定类型的对象
     * @throws IOException 如果发生I/O错误
     */
    public <T> T delete(Class<T> expectType) throws IOException {
        Request req = doBuildRequestBuilder().delete(generationBody()).build();

        try (Response res = call(req)) {
            return parseBody(res, expectType);
        }
    }

    /**
     * 执行HTTP HEAD请求.
     *
     * @return 服务器响应头列表
     * @throws IOException 如果发生I/O错误
     */
    public List<Map.Entry<String, String>> head() throws IOException {
        Request req = doBuildRequestBuilder().head().build();
        try (Response res = call(req)) {
            Spliterator<Pair<String, String>> spliterator =
                Spliterators.spliteratorUnknownSize(
                    res.headers().iterator(), Spliterator.ORDERED | Spliterator.NONNULL);
            return StreamSupport.stream(spliterator, false)
                .map(p -> new AbstractMap.SimpleEntry<>(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());
        }
    }

    /**
     * 执行HTTP POST请求.
     *
     * @param expectType 期望返回的对象类型
     * @param <T>        泛型参数，与expectType相对应的类型
     * @return 服务器响应体被解析为expectType指定类型的对象
     * @throws IOException 如果发生I/O错误
     */
    public <T> T post(Class<T> expectType) throws IOException {
        Request req = doBuildRequestBuilder().post(generationBody()).build();
        try (Response res = call(req)) {
            return parseBody(res, expectType);
        }
    }

    /**
     * 执行HTTP PUT请求.
     *
     * @param expectType 期望返回的对象类型
     * @param <T>        泛型参数，与expectType相对应的类型
     * @return 服务器响应体被解析为expectType指定类型的对象
     * @throws IOException 如果发生I/O错误
     */
    public <T> T put(Class<T> expectType) throws IOException {
        Request req = doBuildRequestBuilder().put(generationBody()).build();
        try (Response res = call(req)) {
            return parseBody(res, expectType);
        }
    }

    /**
     * 执行HTTP PATCH请求.
     *
     * @param expectType 期望返回的对象类型
     * @param <T>        泛型参数，与expectType相对应的类型
     * @return 服务器响应体被解析为expectType指定类型的对象
     * @throws IOException 如果发生I/O错误
     */
    public <T> T patch(Class<T> expectType) throws IOException {
        Request req = doBuildRequestBuilder().patch(generationBody()).build();
        try (Response res = call(req)) {
            return parseBody(res, expectType);
        }
    }

    // 解析响应的body.
    private <T> T parseBody(Response res, Class<T> expectType) throws IOException {
        try (ResponseBody responseBody = res.body()) {
            if (responseBody != null) {
                byte[] data = responseBody.bytes();
                if (returnDeserializer != null) {
                    return returnDeserializer.deserialize(data, expectType);
                } else {
                    // 没有指定,根据MediaTypes类型推算.
                    com.vmsmia.framework.component.rpc.restful.MediaType mediaType =
                        com.vmsmia.framework.component.rpc.restful.MediaType.create(
                            Objects.requireNonNull(responseBody.contentType()).toString());
                    BytesDeserializer deserializer =
                        MediaTypes.getSmartDeserializer(mediaType, expectType).orElse(null);
                    if (deserializer == null) {
                        throw new IllegalArgumentException(
                            String.format("The decoder for the %s media type cannot be inferred.", mediaType));
                    }
                    return deserializer.deserialize(data, expectType);
                }
            } else {
                return null;
            }
        }
    }

    private Request.Builder doBuildRequestBuilder() {
        Request.Builder builder = new Request.Builder().url(generationUrl());
        headers.forEach(entry -> builder.addHeader(entry.getKey(), entry.getValue()));
        return builder;
    }

    private Response call(Request req) throws IOException {
        OkHttpClient client = OK_HTTP_CLIENT.newBuilder()
            .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
            .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
            .build();

        Response res = null;
        try {
            res = client.newCall(req).execute();
            if (!res.isSuccessful()) {
                throw new IOException(
                    String.format(
                        "The request failed with response code %d and message (%s).", res.code(), res.message()
                    ));
            }
            return res;
        } catch (Throwable ex) {
            if (res != null) {
                res.close();
            }
            throw ex;
        }
    }

    private RequestBody generationBody() {
        if (body == null) {
            return RequestBody.create(new byte[0], MediaType.parse(DEFAULT_BODY_MEDIA_TYPE));
        }
        return new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse(bodyMediaType);
            }

            @Override
            public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                if (body instanceof byte[]) {
                    bufferedSink.write((byte[]) body);
                } else if (body instanceof String) {
                    bufferedSink.writeString((String) body, StandardCharsets.UTF_8);
                } else {
                    throw new IllegalArgumentException("Unsupported body type: " + body.getClass());
                }
            }

            @Override
            public long contentLength() throws IOException {
                return super.contentLength();
            }
        };
    }

    private String generationUrl() {
        return this.baseUrl + generatePathFromTemplate();
    }

    /*
    组装请求的uri,包含路径中的变量替换和queryString.
     */
    private String generatePathFromTemplate() {
        if (this.pathTemplate == null || this.pathTemplate.isEmpty()) {
            return this.pathTemplate;
        }

        StringBuilder pathBuff = new StringBuilder();
        StringBuilder variableName = new StringBuilder();
        boolean vigilance = false;
        char currentChar;
        for (int i = 0; i < this.pathTemplate.length(); i++) {
            currentChar = this.pathTemplate.charAt(i);
            if (currentChar == HttpClient.PATH_VARIABLE_PREFIX) {
                vigilance = true;
            }

            if (vigilance) {
                variableName.append(currentChar);
            } else {
                pathBuff.append(currentChar);
            }

            if (currentChar == HttpClient.PATH_VARIABLE_SUFFIX) {
                vigilance = false;
                variableName.deleteCharAt(0);
                variableName.deleteCharAt(variableName.length() - 1);
                String variable = this.pathVariables.get(variableName.toString());
                if (variable != null) {
                    pathBuff.append(variable);
                }
                variableName.setLength(0);
            }
        }

        // 追加QueryString
        String queryString = this.queryParams.entrySet().stream()
            .map(e -> {
                try {
                    return e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }).collect(
                Collectors.joining("&"));

        if (!queryString.isEmpty()) {
            pathBuff.append("?")
                .append(queryString);
        }

        return pathBuff.toString();
    }

    /**
     * 建造者。提供了一系列用于构建和配置HttpClient实例的方法.
     */
    public static final class Builder {
        private String baseUrl = "http://127.0.0.1:8080";
        private String pathTemplate = "";
        private Map<String, String> pathVariables = Collections.emptyMap();
        private Map<String, String> queryParams = Collections.emptyMap();
        private List<Map.Entry<String, String>> headers = Collections.emptyList();
        private Object body;
        private String bodyMediaType = DEFAULT_BODY_MEDIA_TYPE;
        private BytesDeserializer returnDeserializer;
        private long readTimeoutMs = 5000L;
        private long connectTimeoutMs = 5000L;
        private long writeTimeoutMs = 3000L;

        private Builder() {
        }

        public static Builder anBuilder() {
            return new Builder();
        }

        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder withPathTemplate(String pathTemplate) {
            this.pathTemplate = pathTemplate;
            return this;
        }

        public Builder withPathVariables(Map<String, String> pathVariables) {
            this.pathVariables = pathVariables;
            return this;
        }

        public Builder withQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public Builder withHeaders(List<Map.Entry<String, String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder withBody(Object body) {
            this.body = body;
            return this;
        }

        public Builder withBodyMediaType(String bodyMediaType) {
            this.bodyMediaType = bodyMediaType;
            return this;
        }

        public Builder withReturnDeserializer(BytesDeserializer returnDeserializer) {
            this.returnDeserializer = returnDeserializer;
            return this;
        }

        public Builder withReadTimeoutMs(long readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder withConnectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder withWriteTimeoutMs(long writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
            return this;
        }

        /**
         * 构造新的client.
         */
        public HttpClient build() {
            HttpClient httpClient = new HttpClient();
            httpClient.baseUrl = this.baseUrl;
            httpClient.bodyMediaType = this.bodyMediaType;
            httpClient.headers = this.headers;
            httpClient.writeTimeoutMs = this.writeTimeoutMs;
            httpClient.readTimeoutMs = this.readTimeoutMs;
            httpClient.connectTimeoutMs = this.connectTimeoutMs;
            httpClient.body = this.body;
            httpClient.returnDeserializer = this.returnDeserializer;
            httpClient.pathTemplate = this.pathTemplate;
            httpClient.queryParams = this.queryParams;
            httpClient.pathVariables = this.pathVariables;
            return httpClient;
        }
    }
}
