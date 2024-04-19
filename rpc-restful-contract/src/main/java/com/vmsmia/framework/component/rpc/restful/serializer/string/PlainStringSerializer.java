package com.vmsmia.framework.component.rpc.restful.serializer.string;

import com.vmsmia.framework.component.rpc.restful.serializer.StringSerializer;

/**
 * @author bin.dong
 * @version 0.1 2024/4/11 11:04
 * @since 1.8
 */
public class PlainStringSerializer implements StringSerializer {

    private static final PlainStringSerializer INSTANCE = new PlainStringSerializer();

    public static StringSerializer getInstance() {
        return INSTANCE;
    }

    private PlainStringSerializer() {
    }

    @Override
    public String serialize(Object variable) {
        if (variable instanceof String) {
            return (String) variable;
        } else {
            return variable.toString();
        }
    }
}
