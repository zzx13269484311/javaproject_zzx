package com.chat.client;

import java.io.*;
import java.net.Socket;
import com.chat.protocol.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
                    if (content.equals("登录成功")) {
                        controller.loginSuccess();
                    } else if (content.contains("协议错误") || content.contains("昵称已被使用") || content.contains("非法")) {
                        controller.loginFailed();
                        break;
                    } else if (content.equals("服务器已关闭，连接将断开")) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(gui, "服务已关停，群聊室停止营业", "消息", JOptionPane.INFORMATION_MESSAGE);
                            controller.disconnect();
                        });
                        break;
                    } else {
                        gui.appendChat(content);
                    }
                } else if (MessageType.MSG.equals(type)) {
                    gui.appendChat(content);
                } else if (MessageType.KICK.equals(type)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(gui, "由于你违反群聊纪律，被踢出聊天群", "消息", JOptionPane.INFORMATION_MESSAGE);
                        JOptionPane.showMessageDialog(gui, "通信中断，请重新连接", "消息", JOptionPane.INFORMATION_MESSAGE);
                        controller.kickedDisconnect();
                    });
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