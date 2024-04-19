package com.vmsmia.framework.component.rpc.restful.standard.mock;

import com.vmsmia.framework.component.rpc.restful.annotation.RestfulClient;

@RestfulClient("discoer://")
public interface NoAnnotationInterface {

    String call(String name);
}
