package com.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.*;
import java.net.*;

public class WelcomeThread extends Thread {
    private ServerSocket serverSocket;
    private UserManager userManager;
    private ServerGUI gui;

    public WelcomeThread(ServerSocket serverSocket, UserManager userManager, ServerGUI gui) {
        this.serverSocket = serverSocket;
        this.userManager = userManager;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Socket socket = serverSocket.accept();
                gui.appendLog("新客户端连接：" + socket.getInetAddress());
                // 为每个客户端创建一个通信线程
                ClientHandler handler = new ClientHandler(socket, userManager, gui);
                handler.start();
            }
        } catch (IOException e) {
            if (!isInterrupted()) {
                gui.appendLog("迎宾线程异常：" + e.getMessage());
            }
        }
    }
}