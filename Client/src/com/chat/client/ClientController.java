package com.chat.client;

import com.chat.protocol.*;
import java.io.*;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClientController {
    private ClientGUI gui;
    private Socket socket;
    private PrintWriter writer;
    private ReceiveThread receiveThread;
    private String nickname;

    public ClientController(ClientGUI gui) {
        this.gui = gui;
    }

    public void loginFailed() {
        gui.appendChat("登录失败，请检查昵称或服务器设置");
        disconnect();
    }

    public void loginSuccess() {
        gui.setLoggedIn(true);
        // 删除了 gui.appendChat("登录成功！");
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

            receiveThread = new ReceiveThread(socket, gui, this);
            receiveThread.start();

            String loginMsg = MessageUtil.encode(MessageType.LOGIN, nickname);
            writer.println(loginMsg);
            writer.flush();

            this.nickname = nickname;
            // 删除了 gui.appendChat("正在登录...");

        } catch (IOException e) {
            gui.appendChat("连接服务器失败：" + e.getMessage());
        }
    }

    public void logout() {
        if (writer != null) {
            writer.println(MessageUtil.encode(MessageType.QUIT, ""));
        }
        disconnect();
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gui, "您已经离开聊天室", "消息", JOptionPane.INFORMATION_MESSAGE);
        });
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
    }
}