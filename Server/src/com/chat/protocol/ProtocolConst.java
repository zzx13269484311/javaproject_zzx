package com.chat.protocol;

public class ProtocolConst {
    //规定端口号的合法范围
    public static final int MIN_PORT = 1024;
    public static final int MAX_PORT = 65535;
    //如果没有指定，默认使用8888
    public static final int DEFAULT_PORT = 8888;

    //消息中类型与内容之间的分隔符是"|"
    public static final String SEPARATOR = "|";
}