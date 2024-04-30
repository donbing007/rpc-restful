package com.vmsmia.framework.component.rpc.restful.stream;

/**
 * 流式状态响应异常.
 *
 * @author bin.dong
 * @version 0.1 2024/4/25 9:54
 * @since 1.8
 */
public class StreamResponseStatusException extends RuntimeException {

    private final int statusCode;

    public StreamResponseStatusException(int statusCode) {
        super(String.format("Abnormal response to streaming request with response code %d.", statusCode));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
