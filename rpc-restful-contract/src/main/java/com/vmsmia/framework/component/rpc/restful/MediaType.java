package com.vmsmia.framework.component.rpc.restful;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 表示一个媒体类型.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 15:20
 * @since 1.8
 */
public final class MediaType {

    /**
     * 媒体的主类型和子类型的分隔符.
     */
    public static final char TYPE_SEPARATOR = '/';
    /**
     * 参数分隔符.
     */
    public static final char PARAM_SEPARATOR = ';';
    /**
     * 参数键值对分隔符.
     */
    public static final char PARAM_VALUE_SEPARATOR = '=';

    private String type;
    private String subType;
    private List<Map.Entry<String, String>> args;

    private MediaType(String contentType) {
        parse(contentType);
    }

    /**
     * 根据目标 contextType 字符串创建出实际的MediaType实例.
     *
     * @param contentType 目标contextType字符串.
     * @return MediaType实例.
     */
    public static MediaType create(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return MediaTypes.DEFAULT_MEDIA_TYPE;
        }

        return new MediaType(contentType);
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public List<Map.Entry<String, String>> getArgs() {
        return Collections.unmodifiableList(this.args);
    }

    /**
     * 找到名称第一个有效的参数.
     *
     * @param name 参数名称.
     * @return 参数值.
     */
    public Optional<String> getArg(String name) {
        return this.args.stream()
            .filter(arg -> arg.getKey().equals(name))
            .findFirst()
            .map(Map.Entry::getValue);
    }

    public boolean isJson() {
        return "application".equals(this.getType()) && "json".equals(this.getSubType());
    }

    public boolean isText() {
        return "text".equals(this.getType());
    }

    public boolean isXml() {
        return "application".equals(this.getType()) && "xml".equals(this.getSubType())
            || "text".equals(this.getType()) && "xml".equals(this.getSubType());
    }

    public boolean isBinary() {
        return !isJson() && !isText() && !isXml();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaType mediaType = (MediaType) o;
        return Objects.equals(type, mediaType.type) && Objects.equals(subType, mediaType.subType)
            && Objects.equals(args, mediaType.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subType, args);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(type)
            .append(TYPE_SEPARATOR)
            .append(subType);

        if (!this.args.isEmpty()) {
            buff.append(PARAM_SEPARATOR).append(' ');
            buff.append(this.args.stream().map(arg -> arg.getKey() + PARAM_VALUE_SEPARATOR + arg.getValue())
                .collect(Collectors.joining(PARAM_SEPARATOR + " ")));
        }

        return buff.toString();
    }

    // 目标为 application/json; charset=UTF-8; profile="user"; 这样的字符串.
    private void parse(String mediaType) {
        List<String> segment = new LinkedList<>();
        StringBuilder buff = new StringBuilder();
        // 先按";"分割段
        for (int i = 0; i < mediaType.length(); i++) {
            char c = mediaType.charAt(i);
            if (c == PARAM_SEPARATOR) {
                segment.add(buff.toString().trim());
                buff.setLength(0);
            } else {
                buff.append(c);
            }
        }
        if (buff.length() > 0) {
            segment.add(buff.toString());
            buff.setLength(0);
        }

        final int leftValue = 0; // 解析的左值
        final int rightValue = 1; // 解析的右值.
        final int typeIndex = 0;
        // type/subtype
        String[] typeSplitResult = split(segment.get(typeIndex), TYPE_SEPARATOR);
        type = typeSplitResult[leftValue];
        subType = typeSplitResult[rightValue];

        // 无法解析出有效的类型和子类型.
        if (type == null || type.isEmpty() || subType == null || subType.isEmpty()) {
            throw new IllegalArgumentException(String.format("Invalid mediaType string %s.", mediaType));
        }

        segment.remove(typeIndex);

        // args
        if (segment.isEmpty()) {
            this.args = Collections.emptyList();
        } else {
            this.args = new ArrayList<>(segment.size());
            // 参数必须是以=号左右分割的.
            final int argSplitStringSize = 2;
            segment.forEach(a -> {
                String[] argSplitResult = split(a, PARAM_VALUE_SEPARATOR);

                if (argSplitResult.length != argSplitStringSize) {
                    throw new IllegalArgumentException(String.format("Invalid mediaType string %s.", mediaType));
                }

                this.args.add(
                    // 注意这里的key可能在开头有参数分分隔的空格,需要去除.为了最大宽容这里也将值进行上前后空格去除.
                    new AbstractMap.SimpleEntry<>(argSplitResult[leftValue].trim(), argSplitResult[rightValue].trim()));
            });
        }
    }

    private String[] split(String context, char separator) {
        // 返回最大长度.
        final int len = 2;
        final int leftValue = 0; // 解析的左值
        final int rightValue = 1; // 解析的右值.
        String[] results = new String[len];
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < context.length(); i++) {
            char c = context.charAt(i);
            if (c == separator) {
                results[leftValue] = buff.toString();
                buff.setLength(0);
            } else {
                buff.append(c);
            }
        }

        results[rightValue] = buff.toString();
        return results;
    }
}
