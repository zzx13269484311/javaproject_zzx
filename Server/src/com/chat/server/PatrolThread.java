package com.chat.server;

import com.chat.protocol.MessageType;
import com.chat.protocol.MessageUtil;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PatrolThread extends Thread {
    private UserManager userManager;
    private ServerGUI gui;
    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public PatrolThread(UserManager userManager, ServerGUI gui) {
        this.userManager = userManager;
        this.gui = gui;
    }

    public void addMessage(String encodedMessage) {
        messageQueue.offer(encodedMessage);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                String encodedMsg = messageQueue.take(); // 阻塞获取
                String[] parts = MessageUtil.decode(encodedMsg);
                if (parts.length < 2) continue;
                String type = parts[0];
                String content = parts[1];

                // 在服务器 GUI 上显示
                gui.appendLog(content);

                // 广播给所有在线客户端
                broadcast(encodedMsg);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler handler : userManager.getAllHandlers()) {
            handler.sendRawMessage(message);
        }
    }
}