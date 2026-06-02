package com.chat.client;

import com.chat.protocol.*;
import java.io.*;
import java.net.Socket;

public class ClientController {
    private ClientGUI gui;
    private Socket socket;
    private PrintWriter writer;
    private ReceiveThread receiveThread;
    private String nickname;

    public ClientController(ClientGUI gui) {
        this.gui = gui;
    }

    public void login() {
        String ip = gui.getServerIp();
        int port = gui.getPort();
        String nickname = gui.getNickname();
        if (ip.isEmpty() || port < 1024 || port > 65535 || nickname.isEmpty()) {
            gui.appendChat("请完整填写服务器IP、端口(1024-65535)和昵称");
            return;
        }
        try {
            socket = new Socket(ip, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            // 发送登录消息
            writer.println(MessageUtil.encode(MessageType.LOGIN, nickname));
            // 启动接收线程
            receiveThread = new ReceiveThread(socket, gui);
            receiveThread.start();
            this.nickname = nickname;
            gui.setLoggedIn(true);
            gui.appendChat("已连接到服务器，正在登录...");
        } catch (IOException e) {
            gui.appendChat("连接服务器失败：" + e.getMessage());
        }
    }

    public void logout() {
        if (writer != null) {
            writer.println(MessageUtil.encode(MessageType.QUIT, ""));
        }
        disconnect();
    }

    public void sendMessage(String content) {
        if (content.isEmpty()) return;
        writer.println(MessageUtil.encode(MessageType.MSG, content));
        gui.clearMsgField();
    }

    public void disconnect() {
        try {
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) { /* ignore */ }
        if (receiveThread != null) receiveThread.interrupt();
        gui.setLoggedIn(false);
        gui.appendChat("已断开连接");
    }
}