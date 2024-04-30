package com.vmsmia.framework.component.rpc.restful.stream;

import com.vmsmia.framework.component.rpc.restful.MediaType;
import java.nio.ByteBuffer;

/**
 * 流式推送的监听器.
 *
 * @author bin.dong
 * @version 0.1 2024/4/22 18:04
 * @since 1.8
 */
public interface StreamSubscriber {

    /**
     * 流的读取处理缓冲大小,单位字节数量.
     */
    int readBuffSize();

    /**
     * 准备完成时处理.
     *
     * @param mediaType 流媒体类型.
     */
    void onStart(MediaType mediaType);

    /**
     * 收到数据的响应.根据处理响应决定是否继续处理后续消息还是关闭流.
     *
     * @return true 继续处理后续消息, false 取消流.
     */
    boolean onNext(ByteBuffer data);

    /**
     * 以错误结束,之后不会再有消息推送.
     */
    void onError(Throwable t);

    /**
     * 正常结束,之后不会再有消息推送.
     */
    void onComplete();

    /**
     * onNext 返回为false时被调用.
     */
    void onCancel();
}
