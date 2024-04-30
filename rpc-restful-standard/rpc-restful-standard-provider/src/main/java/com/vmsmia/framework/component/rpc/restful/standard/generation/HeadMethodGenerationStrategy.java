package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;
import java.util.List;
import java.util.Map;

/**
 * {@code HeadMethodGenerationStrategy} 类是 {@code AbstractMethodGenerationStrategy} 的一个具体实现，
 * 专门用于生成处理 HTTP HEAD 请求的代码.此类通过覆盖 {@code finishedCall} 方法，实现了针对 HTTP HEAD 请求的特定代码生成逻辑.
 *
 * <p>HTTP HEAD 方法类似于 GET 方法，但它不返回消息体.它通常被用于获取请求的头信息，例如校验资源的最新性、存在性以及获取资源的元数据等.
 * {@code HeadMethodGenerationStrategy} 类根据提供的接口方法和注解信息，自动生成执行 HTTP HEAD 请求的客户端代码.</p>
 *
 * <h3>核心功能</h3>
 * <ul>
 *     <li>解析接口方法和注解，生成对应的 HTTP HEAD 请求代码.</li>
 *     <li>通过动态反射机制，适配返回类型，以适应不同的响应数据处理需求.</li>
 *     <li>支持自定义 HTTP 客户端配置，包括请求头、查询参数等.</li>
 * </ul>
 *
 * <h3>代码生成逻辑</h3>
 * <p>{@code finishedCall} 方法中，利用传入的 {@code httpClientVariableName}（HTTP 客户端的变量名称），
 * 来调用 HTTP 客户端的 {@code head} 方法，执行实际的 HTTP HEAD 请求.
 * 请求的结果（通常为响应头信息），将被赋值给 {@code callResultVariableName} 变量，
 * 此变量的类型被指定为 {@code List<Map.Entry<String, String>>}，这表示响应头信息以键值对列表的形式返回.</p>
 *
 * <p>使用 {@code HeadMethodGenerationStrategy} 类时，开发者无需关心具体的 HTTP 请求发送细节，
 * 只需关注接口方法的定义与注解配置，就能自动获取到 HEAD 请求的响应头信息.</p>
 *
 * <h3>注意事项</h3>
 * <ul>
 *     <li>确保使用兼容的 HTTP 客户端代码结构，以支持 {@code head} 方法调用.</li>
 *     <li>对于返回类型与预期不符的情况，需在调用处进行适当的类型转换或错误处理.</li>
 *     <li>考虑到 HEAD 请求仅用于获取响应头信息，返回的数据结构与实际响应体无关，应避免在需要响应体内容的场景中使用 HEAD 请求.</li>
 * </ul>
 *
 * @author bin.dong
 * @version 0.1 2024/4/15 17:46
 * @since 1.8
 */
public class HeadMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // List<Map.Entry<String, String>> {callResultVariableName} = {httpClientVariableName}.head();

        return CodeBlock.builder()
            .addStatement(
                "$T<$T<$T, $T>> $L = $L.head()",
                List.class,
                Map.Entry.class,
                String.class,
                String.class,
                callResultVariableName,
                httpClientVariableName)
            .build();
    }
}
