package com.chat.server;
import com.chat.protocol.ProtocolConst;
import java.net.ServerSocket;
import java.io.IOException;

public class ServerController {
    private ServerGUI gui;
    private ServerSocket serverSocket;
    private WelcomeThread welcomeThread;
    private PatrolThread patrolThread;
    private UserManager userManager;
    private volatile boolean isRunning = false;

    public ServerController(ServerGUI gui) {
        this.gui = gui;
        this.userManager = new UserManager();
    }

    public void startServer() {
        int port = gui.getPort();
        if (port < ProtocolConst.MIN_PORT || port > ProtocolConst.MAX_PORT) {
            gui.appendLog("端口号无效，请输入 " + ProtocolConst.MIN_PORT + "~" + ProtocolConst.MAX_PORT);
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            patrolThread = new PatrolThread(userManager, gui);
            patrolThread.start();
            welcomeThread = new WelcomeThread(serverSocket, userManager, gui, patrolThread);
            welcomeThread.start();
            gui.appendLog("服务器已启动，监听端口：" + port);
            gui.setStartEnabled(false);
        } catch (IOException e) {
            gui.appendLog("启动失败：" + e.getMessage());
        }
    }

    public void stopServer() {
        isRunning = false;
        // 关闭 ServerSocket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) { /* ignore */ }
        }
        // 中断线程
        if (welcomeThread != null) welcomeThread.interrupt();
        if (patrolThread != null) patrolThread.interrupt();
        // 断开所有客户端连接（稍后实现）
        gui.appendLog("服务器已停止");
        gui.setStartEnabled(true);
    }

    public void refreshUserList() {
        String[] users = userManager.getAllNicknames();
        gui.updateUserList(users);
    }

    public void kickUser() {
        // 稍后实现
    }
}