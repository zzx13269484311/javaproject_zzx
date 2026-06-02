package com.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class WelcomeThread extends Thread {
    private ServerSocket serverSocket;
    private UserManager userManager;
    private ServerGUI gui;
    private PatrolThread patrolThread;

    public WelcomeThread(ServerSocket serverSocket, UserManager userManager, ServerGUI gui, PatrolThread patrolThread) {
        this.serverSocket = serverSocket;
        this.userManager = userManager;
        this.gui = gui;
        this.patrolThread = patrolThread;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Socket socket = serverSocket.accept();
                // 删除：gui.appendLog("新客户端连接：" + socket.getInetAddress());
                ClientHandler handler = new ClientHandler(socket, userManager, gui, patrolThread);
                handler.start();
            }
        } catch (IOException e) {
            if (!isInterrupted()) {
                gui.appendLog("迎宾线程异常：" + e.getMessage());
            }
        }
    }
}