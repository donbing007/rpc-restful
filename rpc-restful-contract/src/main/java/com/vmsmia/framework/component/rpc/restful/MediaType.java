package com.vmsmia.framework.component.rpc.restful;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表示一个媒体类型.
 *
 * @author bin.dong
 * @version 0.1 2024/4/16 15:20
 * @since 1.8
 */
public final class MediaType {

    private static final Pattern MEDIA_TYPE_STRING_PATTERN =
        Pattern.compile("^[a-zA-Z]+/[a-zA-Z0-9\\-+.]+(?:;\\s*[a-zA-Z0-9\\-]+=[a-zA-Z0-9\\-]+)?$");
    private static final char TYPE_SEPARATOR = '/';
    private static final char PARAM_SEPARATOR = ';';
    private static final char PARAM_VALUE_SEPARATOR = '=';

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
        String useContentType = contentType.trim().toLowerCase();
        assert MEDIA_TYPE_STRING_PATTERN != null;
        Matcher matcher = MEDIA_TYPE_STRING_PATTERN.matcher(useContentType);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid media type %s format. Expected 'type/subtype' or 'type/subtype; k=v'.", useContentType));

        }
        return new MediaType(contentType.toLowerCase());
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
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(type)
            .append(TYPE_SEPARATOR)
            .append(subType);

        if (!this.args.isEmpty()) {
            buff.append(PARAM_SEPARATOR).append(' ');
            for (Map.Entry<String, String> arg : this.args) {
                buff.append(arg.getKey())
                    .append(PARAM_VALUE_SEPARATOR)
                    .append(arg.getValue())
                    .append(PARAM_SEPARATOR)
                    .append(' ');
            }
        }

        return buff.toString();
    }

    // 目标为 application/json; charset=UTF-8; profile="user"; 这样的字符串.
    private void parse(String contentType) {
        List<String> segment = new LinkedList<>();
        StringBuilder buff = new StringBuilder();
        // 先按";"分割段
        for (int i = 0; i < contentType.length(); i++) {
            char c = contentType.charAt(i);
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

        segment.remove(typeIndex);

        // args
        if (segment.isEmpty()) {
            this.args = Collections.emptyList();
        } else {
            this.args = new ArrayList<>(segment.size());
            segment.forEach(a -> {
                String[] argSplitResult = split(a, PARAM_VALUE_SEPARATOR);
                this.args.add(new AbstractMap.SimpleEntry<>(argSplitResult[leftValue], argSplitResult[rightValue]));
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
                results[leftValue] = buff.toString().toLowerCase();
                buff.setLength(0);
            } else {
                buff.append(c);
            }
        }

        results[rightValue] = buff.toString().toLowerCase();
        return results;
    }
}
