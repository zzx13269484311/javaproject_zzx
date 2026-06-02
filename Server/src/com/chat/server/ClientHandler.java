package com.chat.server;

import java.io.*;
import java.net.Socket;
import com.chat.protocol.*;
import javax.swing.SwingUtilities;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler extends Thread {
    private Socket socket;
    private UserManager userManager;
    private ServerGUI gui;
    private String nickname;
    private BufferedReader reader;
    private PrintWriter writer;
    private PatrolThread patrolThread;

    public ClientHandler(Socket socket, UserManager userManager, ServerGUI gui, PatrolThread patrolThread) {
        this.socket = socket;
        this.userManager = userManager;
        this.gui = gui;
        this.patrolThread = patrolThread;
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
            sendMessage(MessageType.SYS, "登录成功");

            // 刷新服务器用户列表
            SwingUtilities.invokeLater(() -> {
                String[] users = userManager.getAllNicknames();
                gui.updateUserList(users);
            });

            // 广播上线通知
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            String sysMsgContent = String.format("【%s,%s】：【进入了群聊室】", timestamp, nickname);
            String sysMsg = MessageUtil.encode(MessageType.SYS, sysMsgContent);
            patrolThread.addMessage(sysMsg);

            // 循环读取消息
            String line;
            while ((line = reader.readLine()) != null) {
                parts = MessageUtil.decode(line);
                if (parts.length < 2) continue;
                String type = parts[0];
                String content = parts[1];
                if (MessageType.MSG.equals(type)) {
                    timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    String broadcastContent = String.format("【%s,%s】：%s", timestamp, nickname, content);
                    String broadcastMsg = MessageUtil.encode(MessageType.MSG, broadcastContent);
                    patrolThread.addMessage(broadcastMsg);
                } else if (MessageType.QUIT.equals(type)) {
                    break;
                }
            }
        } catch (IOException e) {
            gui.appendLog("客户端 " + nickname + " 连接异常: " + e.getMessage());
        } finally {
            close();
            if (nickname != null) {
                String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String sysMsgContent = String.format("【%s,%s】：【离开了群聊室】", timestamp, nickname);
                String sysMsg = MessageUtil.encode(MessageType.SYS, sysMsgContent);
                patrolThread.addMessage(sysMsg);
                userManager.removeUser(nickname);
                SwingUtilities.invokeLater(() -> {
                    String[] users = userManager.getAllNicknames();
                    gui.updateUserList(users);
                });
            }
        }
    }

    public void sendMessage(String type, String content) {
        if (writer != null) {
            writer.println(MessageUtil.encode(type, content));
        }
    }

    public void close() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) { /* ignore */ }
    }

    public void sendRawMessage(String raw) {
        if (writer != null) {
            writer.println(raw);
        }
    }

    public boolean isSocketAlive() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public String getNickname() { return nickname; }
}