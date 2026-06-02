package com.chat.server;
import com.chat.protocol.ProtocolConst;
import com.chat.protocol.MessageType;
import com.chat.protocol.MessageUtil;
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
        // 广播服务器关闭消息
        String shutdownMsg = MessageUtil.encode(MessageType.SYS, "服务器已关闭，连接将断开");
        for (ClientHandler handler : userManager.getAllHandlers()) {
            handler.sendRawMessage(shutdownMsg);
            handler.close();
        }
        userManager.getAllNicknames(); // 清空（如果需要）
        // 关闭 ServerSocket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) { /* ignore */ }
        }
        // 中断线程
        if (welcomeThread != null) welcomeThread.interrupt();
        if (patrolThread != null) patrolThread.interrupt();
        gui.appendLog("服务器已停止");
        gui.setStartEnabled(true);
    }

    public void refreshUserList() {
        String[] users = userManager.getAllNicknames();
        gui.updateUserList(users);
    }

    public void kickUser() {
        String nickname = gui.getKickNickname();
        if (nickname == null || nickname.trim().isEmpty()) {
            gui.appendLog("踢人失败：昵称不能为空");
            return;
        }
        ClientHandler handler = userManager.getHandler(nickname);
        if (handler == null) {
            gui.appendLog("踢人失败：用户 " + nickname + " 不在线");
            return;
        }
        // 发送被踢通知
        handler.sendMessage(MessageType.KICK, "您已被管理员踢出聊天室");
        // 关闭连接并清理资源（handler 内部会关闭 socket 并移除用户）
        handler.close();
        userManager.removeUser(nickname);
        // 刷新在线列表
        gui.updateUserList(userManager.getAllNicknames());
        // 广播系统消息
        String sysMsg = MessageUtil.encode(MessageType.SYS, nickname + " 被管理员踢出聊天室");
        patrolThread.addMessage(sysMsg);
        gui.appendLog("管理员踢出了用户：" + nickname);
        gui.clearKickField();
    }
}