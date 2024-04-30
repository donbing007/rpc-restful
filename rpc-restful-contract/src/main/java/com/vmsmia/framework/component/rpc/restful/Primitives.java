package com.vmsmia.framework.component.rpc.restful;

/**
 * 基础类型的帮助类.
 *
 * @author bin.dong
 * @version 0.1 2024/4/18 18:49
 * @since 1.8
 */
public class Primitives {

    private Primitives() {

    }

    /**
     * 判断是否为基础类型的包覆类型.
     *
     * @param type 目标类型.
     * @return true 是, false 不是.
     */
    public static boolean isWrapperType(Class<?> type) {
        return (type == Integer.class)
            || (type == Long.class)
            || (type == Byte.class)
            || (type == Short.class)
            || (type == Character.class)
            || (type == Boolean.class)
            || (type == Double.class)
            || (type == Float.class);
    }

    /**
     * 判断是否是基础类型.
     *
     * @param type 目标类型.
     * @return true 是, false 不是.
     */
    public static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || Primitives.isWrapperType(type);
    }
}
