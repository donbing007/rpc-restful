package com.vmsmia.framework.component.rpc.restful.standard.mock;

import com.vmsmia.framework.component.rpc.restful.annotation.Path;
import com.vmsmia.framework.component.rpc.restful.annotation.RequestHead;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.annotation.Timeout;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Stream;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.PathVariable;
import com.vmsmia.framework.component.rpc.restful.stream.StreamSubscriber;

@RestfulClient("discover://test")
public interface StreamAnnotationInterface {

    @Stream
    @Path("/{name}/get/{id}")
    @RequestHead(key = "t1", val = "test")
    @RequestHead(key = "t2", val = "test")
    @Timeout(readTimeoutMs = 100, connectTimeoutMs = 100, writeTimeoutMs = 100)
    void call(@PathVariable String name, @PathVariable long id, StreamSubscriber subscriber);
}
