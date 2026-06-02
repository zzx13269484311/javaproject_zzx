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
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gui,
                    "昵称不符合规范，或者昵称在群聊室里已存在，请尝试其他昵称",
                    "消息",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        disconnect();
    }

    public void loginSuccess() {
        gui.setLoggedIn(true);
        // 弹窗提示
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(gui, "您已经成功进入群聊室，开始聊天吧", "消息", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void login() {
        String ip = gui.getServerIp();
        int port = gui.getPort();
        String nickname = gui.getNickname().trim();   // 先去除前后空格

        // 昵称合法性检查（空字符串 或 等于"管理员"）
        if (nickname.isEmpty() || "管理员".equals(nickname)) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui,
                        "昵称不符合规范，或者昵称在群聊室里已存在，请尝试其他昵称",
                        "消息",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            return;
        }

        //  IP 和端口合法性检查
        if (ip.isEmpty() || port < 1024 || port > 65535) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui,
                        "请完整填写服务器IP和端口(1024-65535)",
                        "消息",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            return;
        }

        // 连接服务器
        try {
            socket = new Socket(ip, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            receiveThread = new ReceiveThread(socket, gui, this);
            receiveThread.start();
            String loginMsg = MessageUtil.encode(MessageType.LOGIN, nickname);
            writer.println(loginMsg);
            writer.flush();
            this.nickname = nickname;
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(gui,
                        "连接服务器失败：" + e.getMessage(),
                        "消息",
                        JOptionPane.ERROR_MESSAGE);
            });
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

    private void disconnectInternal(boolean showMessage) {
        try {
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) { /* ignore */ }
        if (receiveThread != null) receiveThread.interrupt();
        gui.setLoggedIn(false);
        if (showMessage) {
            gui.appendChat("已断开连接");
        }
    }

    public void disconnect() {
        disconnectInternal(true);
    }

    public void kickedDisconnect() {
        disconnectInternal(false);
    }
}