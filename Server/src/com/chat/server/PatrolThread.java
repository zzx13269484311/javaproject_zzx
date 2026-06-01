package com.chat.server;

public class PatrolThread extends Thread {
    private UserManager userManager;
    private ServerGUI gui;

    public PatrolThread(UserManager userManager, ServerGUI gui) {
        this.userManager = userManager;
        this.gui = gui;
    }

    @Override
    public void run() {
        // 第二天实现消息队列广播和心跳检测
    }
}