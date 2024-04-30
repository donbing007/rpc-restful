package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.squareup.javapoet.CodeBlock;

/**
 * {@code DeleteMethodGenerationStrategy} 类继承自 {@code AbstractMethodGenerationStrategy}，提供了一个特定于 HTTP DELETE 请求的代码生成策略.
 * 通过覆写 {@code finishedCall} 方法, 它实现了利用 OkHttp 的 delete 方法来构造 HTTP DELETE 调用的逻辑.
 * <p>
 * 在旨在从接口定义自动生成 HTTP 客户端代码的 RPC 框架中, 当接口方法通过注解指定为 DELETE 操作时, {@code DeleteMethodGenerationStrategy} 将介入处理.
 * 该策略负责构建 HTTP DELETE 请求的具体实现代码, 并且确保请求的执行符合通过注解定义的接口方法的预期行为.
 * </p>
 * <p>
 * 该策略的关键在于 {@code finishedCall} 方法的实现. 该方法使用传入的 {@code httpClientVariableName} 变量名称来调用 OkHttp 的 delete 方法,
 * 并通过反射（{@code Class.forName(returnTypeFqn)}）来动态确定返回类型. 生成的代码片段将请求的结果赋值给 {@code callResultVariableName} 变量,
 * 允许后续流程根据请求的结果来进行相应的处理.
 * </p>
 * <p>
 * 注意：此策略假定所有 DELETE 请求不携带请求体, 并且预期服务器的响应将直接映射到指定的返回类型上. 使用者应确保返回类型与实际的响应兼容,
 * 并处理可能出现的类型转换异常.
 * </p>
 *
 * @author bin.dong
 * @version 0.1 2024/4/15 17:46
 * @since 1.8
 */
public class DeleteMethodGenerationStrategy extends AbstractMethodGenerationStrategy {

    @Override
    protected CodeBlock finishedCall(String httpClientVariableName, String callResultVariableName,
                                     String returnTypeFqn) {
        // {returnTypeFqn} {callResultVariableName} = {httpClientVariableName}.delete(Class.forName("{returnTypeFqn}"));
        return CodeBlock.builder()
            .addStatement("$L = ($L) $L.delete(Class.forName($S))",
                callResultVariableName, returnTypeFqn, httpClientVariableName, returnTypeFqn)
            .build();
    }
}
