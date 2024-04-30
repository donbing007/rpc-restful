package com.vmsmia.framework.component.rpc.restful.standard.mock;

import com.vmsmia.framework.component.rpc.restful.annotation.Path;
import com.vmsmia.framework.component.rpc.restful.annotation.RequestHead;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Get;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.PathVariable;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.QueryParam;

@RestfulClient("discover://test")
public interface GetAnnotationInterface {

    @Get
    @Path("/{name}/get/{id}")
    @RequestHead(key = "t1", val = "test")
    @RequestHead(key = "t2", val = "test")
    String call(@PathVariable("name") String n, @PathVariable String id, @QueryParam String type);
}
