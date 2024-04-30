package com.vmsmia.framework.component.rpc.restful.common.exception;

/**
 * @author bin.dong
 * @version 0.1 2024/4/15 18:02
 * @since 1.8
 */
public class RestfulException extends RuntimeException {

    public RestfulException() {
    }

    public RestfulException(String message) {
        super(message);
    }

    public RestfulException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestfulException(Throwable cause) {
        super(cause);
    }

    public RestfulException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
