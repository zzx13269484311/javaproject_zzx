package com.chat.server;

import java.io.*;
import java.net.Socket;
import com.chat.protocol.*;
import com.chat.protocol.MessageUtil;
import com.chat.protocol.MessageType;
import java.io.*;


public class ClientHandler extends Thread {
    private Socket socket;
    private UserManager userManager;
    private ServerGUI gui;
    private String nickname;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket socket, UserManager userManager, ServerGUI gui) {
        this.socket = socket;
        this.userManager = userManager;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // 读取第一条消息（必须是 LOGIN）
            String firstLine = reader.readLine();
            if (firstLine == null) return;
            String[] parts = MessageUtil.decode(firstLine);
            if (parts.length != 2 || !MessageType.LOGIN.equals(parts[0])) {
                sendMessage(MessageType.SYS, "协议错误，请先登录");
                close();
                return;
            }
            String reqNickname = parts[1];
            if (!userManager.addUser(reqNickname, this)) {
                sendMessage(MessageType.SYS, "昵称已被使用或非法");
                close();
                return;
            }
            this.nickname = reqNickname;
            gui.appendLog("用户 " + nickname + " 加入聊天室");
            // 广播上线通知（后续巡检线程负责，暂时先不做）
            // 之后循环读取消息
            String line;
            while ((line = reader.readLine()) != null) {
                parts = MessageUtil.decode(line);
                if (parts.length < 2) continue;
                String type = parts[0];
                String content = parts[1];
                if (MessageType.MSG.equals(type)) {
                    // 暂时只打印，后续放入队列广播
                    gui.appendLog(nickname + " 说: " + content);
                } else if (MessageType.QUIT.equals(type)) {
                    break;
                }
            }
        } catch (IOException e) {
            gui.appendLog("客户端 " + nickname + " 连接异常: " + e.getMessage());
        } finally {
            close();
            if (nickname != null) {
                userManager.removeUser(nickname);
                gui.appendLog("用户 " + nickname + " 已退出");
            }
            // 广播下线通知（后续实现）
        }
    }

    public void sendMessage(String type, String content) {
        if (writer != null) {
            writer.println(MessageUtil.encode(type, content));
        }
    }

    private void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) { /* ignore */ }
    }
}