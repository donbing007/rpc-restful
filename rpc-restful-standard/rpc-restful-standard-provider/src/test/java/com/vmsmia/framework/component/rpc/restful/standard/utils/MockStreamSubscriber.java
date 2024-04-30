package com.vmsmia.framework.component.rpc.restful.standard.utils;

import com.vmsmia.framework.component.rpc.restful.MediaType;
import com.vmsmia.framework.component.rpc.restful.stream.StreamSubscriber;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author bin.dong
 * @version 0.1 2024/4/25 15:57
 * @since 1.8
 */
public class MockStreamSubscriber implements StreamSubscriber {

    private ByteArrayOutputStream buff = new ByteArrayOutputStream();
    private Throwable error;
    private MediaType mediaType;

    private volatile boolean completed;
    private volatile boolean errorCompleted;
    private volatile boolean canceled;

    private final int readBufferSize;

    public MockStreamSubscriber(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public boolean isFinished() {
        return completed || errorCompleted || canceled;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isErrorCompleted() {
        return errorCompleted;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public String getStringValue() {
        return new String(buff.toByteArray(), StandardCharsets.UTF_8);
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public int readBuffSize() {
        return readBufferSize;
    }

    @Override
    public void onStart(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public boolean onNext(ByteBuffer data) {
        buff.write(data.array(), 0, data.remaining());

        return true;
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
        this.errorCompleted = true;
    }

    @Override
    public void onComplete() {
        this.completed = true;
    }

    @Override
    public void onCancel() {
        this.canceled = true;
    }
}
