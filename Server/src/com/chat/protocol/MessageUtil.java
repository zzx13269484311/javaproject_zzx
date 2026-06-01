package com.chat.protocol;

public class MessageUtil {
    // 编码：类型 + 分隔符 + 内容
    public static String encode(String type, String content) {
        return type + ProtocolConst.SEPARATOR + content;
    }

    // 解码：返回数组 [type, content]
    public static String[] decode(String message) {
        return message.split(ProtocolConst.SEPARATOR, 2);
    }
}