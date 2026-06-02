package com.chat.protocol;

public class MessageUtil {
    public static String encode(String type, String content) {
        return type + ProtocolConst.SEPARATOR + content;
    }

    public static String[] decode(String message) {
        return message.split(ProtocolConst.SEPARATOR, 2);
    }
}