package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;

/**
 * {@code GetMethodGenerationStrategy} 类继承自 {@code AbstractMethodGenerationStrategy}，
 * 专门提供了对 HTTP GET 请求的代码生成策略.
 * 它通过覆写 {@code finishedCall} 方法来实现，依赖 OkHttp 的 get 方法构建 HTTP GET 调用代码.
 * <p>
 * 在自动化 HTTP 客户端代码生成框架中，当一个接口方法被注解以表示一个 GET 操作时，{@code GetMethodGenerationStrategy} 被用来处理这种情况.
 * 该策略负责构建适合执行 HTTP GET 请求的代码，确保通过接口定义的方法能够按预期方式执行 HTTP 请求.
 * </p>
 * <p>
 * 此策略核心实现在于 {@code finishedCall} 方法.该方法利用传入的
 * {@code httpClientVariableName}（HttpClient 变量名）执行 OkHttp 库的 get 方法，
 * 并动态地通过 {@code Class.forName(returnTypeFqn)} 确定方法的返回类型.生成的代码将使请求结果直接赋值给
 * {@code callResultVariableName} 变量，
 * 允许进一步处理请求返回的数据.
 * </p>
 * <p>
 * 注意：该策略默认 GET 请求不应携带请求体，并期待服务器的响应可以直接映射到指定的返回类型.使用者应保证所指示的返回类型与实际服务器响应兼容，
 * 并且能够妥善处理可能发生的类型不匹配异常.
 * </p>
 *
 * @author bin.dong
 * @version 0.1 2024/4/12 11:14
 * @since 1.8
 */
public class GetMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // {returnTypeFqn} {callResultVariableName} = {httpClientVariableName}.get(Class.forName("{returnTypeFqn}"));
        return CodeBlock.builder()
            .addStatement("$L = ($L) $L.get(Class.forName($S))",
                callResultVariableName, returnTypeFqn, httpClientVariableName, returnTypeFqn)
            .build();
    }
}
