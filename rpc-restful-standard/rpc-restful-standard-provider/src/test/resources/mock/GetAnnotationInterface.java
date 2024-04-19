package com.vmsmia.framework.component.rpc.restful.standard.mock;

import com.vmsmia.framework.component.rpc.restful.annotation.Path;
import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;
import com.vmsmia.framework.component.rpc.restful.annotation.ReturnDeserializer;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Get;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.PathVariable;
import com.vmsmia.framework.component.rpc.restful.annotation.parameter.QueryParam;
import com.vmsmia.framework.component.rpc.restful.serializer.string.PlainStringDeserializer;

@RestfulClient("discover://test")
public interface GetAnnotationInterface {

    @Get
    @Path("/{name}/get/{id}")
    String call(@PathVariable("name") String n, @PathVariable String id, @QueryParam String type);
}
