package com.chat.server;

import com.chat.protocol.MessageType;
import com.chat.protocol.MessageUtil;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        long lastCheck = System.currentTimeMillis();
        while (!isInterrupted()) {
            try {
                String encodedMsg = messageQueue.poll(1, TimeUnit.SECONDS);
                if (encodedMsg != null) {
                    String[] parts = MessageUtil.decode(encodedMsg);
                    if (parts.length >= 2) {
                        String content = parts[1];
                        gui.appendLog(content);
                        broadcast(encodedMsg);
                    }
                }
                long now = System.currentTimeMillis();
                if (now - lastCheck >= 30000) {
                    lastCheck = now;
                    checkClientAlive();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void checkClientAlive() {
        for (ClientHandler handler : userManager.getAllHandlers()) {
            if (!handler.isSocketAlive()) {
                String nickname = handler.getNickname();
                handler.close();
                userManager.removeUser(nickname);
                gui.updateUserList(userManager.getAllNicknames());
                String sysMsg = MessageUtil.encode(MessageType.SYS, nickname + " 异常断开");
                addMessage(sysMsg);
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler handler : userManager.getAllHandlers()) {
            handler.sendRawMessage(message);
        }
    }
}