package com.vmsmia.framework.component.rpc.restful.common;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 随机字符串生成帮助工具.
 *
 * @author bin.dong
 * @version 0.1 2024/4/25 16:13
 * @since 1.8
 */
public class RandomUtils {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成指定字节范围的随机字符串.
     */
    public static String generateRandomString(int minBytes, int maxBytes) {
        if (minBytes < 0 || maxBytes < 0 || minBytes > maxBytes) {
            throw new IllegalArgumentException("Invalid range: [" + minBytes + ", " + maxBytes + "]");
        }
        int length = minBytes + ThreadLocalRandom.current().nextInt(maxBytes - minBytes + 1);
        return IntStream.range(0, length)
            .mapToObj(i -> String.valueOf(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length()))))
            .collect(Collectors.joining());
    }

    /**
     * 生成指定范围的随机整数.
     */
    public static int generateRandomInt(int min, int max) {
        if (min < 0 || max < 0 || min > max) {
            throw new IllegalArgumentException("Invalid range: [" + min + ", " + max + "]");
        }
        return min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

    /**
     * 生成随机的bool型.
     */
    public static boolean generateRandomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
