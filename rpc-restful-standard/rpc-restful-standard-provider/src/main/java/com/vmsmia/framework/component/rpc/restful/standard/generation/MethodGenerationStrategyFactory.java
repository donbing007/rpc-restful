package com.vmsmia.framework.component.rpc.restful.standard.generation;

import com.vmsmia.framework.component.rpc.restful.annotation.method.Delete;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Get;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Head;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Patch;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Post;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Put;
import com.vmsmia.framework.component.rpc.restful.annotation.method.Stream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * @author bin.dong
 * @version 0.1 2024/4/9 15:13
 * @since 1.8
 */
public class MethodGenerationStrategyFactory {

    private static final Map<String, MethodGenerationStrategy> STRATEGY_MAP;

    static {
        // FQN 为key.
        STRATEGY_MAP = new HashMap<>();
        STRATEGY_MAP.put(Get.class.getName(), new GetMethodGenerationStrategy());
        STRATEGY_MAP.put(Post.class.getName(), new PostMethodGenerationStrategy());
        STRATEGY_MAP.put(Delete.class.getName(), new DeleteMethodGenerationStrategy());
        STRATEGY_MAP.put(Head.class.getName(), new HeadMethodGenerationStrategy());
        STRATEGY_MAP.put(Patch.class.getName(), new PatchMehtodGenerationStrategy());
        STRATEGY_MAP.put(Put.class.getName(), new PutMethodGenerationStrategy());
        STRATEGY_MAP.put(Stream.class.getName(), new StreamMethodGenerationStrategy());
    }

    public static MethodGenerationStrategy getStrategy(ExecutableElement el) {
        /*
            这里需要得到注解的FQN,但是 DeclaredType.toString() 依赖实现有不确定性.
            转换成 TypeElement.getQualifiedName().toString() 保证一定得到预期的注解FQN.
        */
        return el.getAnnotationMirrors()
            .stream()
            .map(am -> ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString())
            .map(STRATEGY_MAP::get)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(NoSupportGenerationStrategy.getInstance());
    }
}
