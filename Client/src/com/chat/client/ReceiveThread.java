package com.chat.client;

import java.io.*;
import java.net.Socket;
import com.chat.protocol.*;

public class ReceiveThread extends Thread {
    private Socket socket;
    private ClientGUI gui;
    private ClientController controller;
    private BufferedReader reader;

    public ReceiveThread(Socket socket, ClientGUI gui, ClientController controller) {
        this.socket = socket;
        this.gui = gui;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while (!isInterrupted() && (line = reader.readLine()) != null) {
                String[] parts = MessageUtil.decode(line);
                if (parts.length < 2) continue;
                String type = parts[0];
                String content = parts[1];

                if (MessageType.SYS.equals(type)) {
                    // 不显示“登录成功”这条系统消息
                    if (!content.equals("登录成功")) {
                        gui.appendChat(content);
                    }
                    if (content.contains("协议错误") || content.contains("昵称已被使用") || content.contains("非法")) {
                        controller.loginFailed();
                        break;
                    }
                    if (content.equals("登录成功")) {
                        controller.loginSuccess();
                    }
                } else if (MessageType.MSG.equals(type)) {
                    gui.appendChat(content);
                } else if (MessageType.KICK.equals(type)) {
                    gui.appendChat("系统通知：" + content);
                    controller.disconnect();
                    break;
                }
            }
        } catch (IOException e) {
            if (!isInterrupted()) {
                gui.appendChat("与服务器断开连接");
                controller.disconnect();
            }
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) { /* ignore */ }
        }
    }
}