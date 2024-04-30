package com.vmsmia.framework.component.rpc.restful.annotation.method;

import com.vmsmia.framework.component.rpc.restful.annotation.ReturnDeserializer;
import com.vmsmia.framework.component.rpc.restful.stream.StreamSubscriber;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这是一个特殊的method, 其不是标准http的method.<br>
 * 其用以标示当前方法是处理服务端推送的接口.其对接口有如下要求.<br>
 * <ul>
 *     <li>接口必须是void响应.</li>
 *     <li>入参中必须包含一个且只能有一个实现了{@link StreamSubscriber}的入参.</li>
 *     <li>可以使用和GET请求一样入参.</li>
 *     <li>同样也可以指定{@link ReturnDeserializer},这里表示每一次收到的推送的数据如何解码
 *     .</li>
 * </ul>
 *
 * @author bin.dong
 * @version 0.1 2024/4/22 18:11
 * @since 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Stream {
}
