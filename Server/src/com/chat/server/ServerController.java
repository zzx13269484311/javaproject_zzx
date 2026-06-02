package com.chat.server;
import com.chat.protocol.ProtocolConst;
import com.chat.protocol.MessageType;
import com.chat.protocol.MessageUtil;
import java.net.ServerSocket;
import java.io.IOException;
import java.text.SimpleDateFormat;   // 新增
import java.util.Date;               // 新增
import javax.swing.JOptionPane;

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
            // 删除：gui.appendLog("服务器已启动，监听端口：" + port);
            gui.setStartEnabled(false);
            gui.setStartEnabled(false);
            // 弹出提示窗口
            JOptionPane.showMessageDialog(gui, "服务已启动，可以接入客户端连接了", "消息", JOptionPane.INFORMATION_MESSAGE);
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
        if (nickname == null || nickname.trim(). isEmpty()) {
            JOptionPane.showMessageDialog(gui, "要踢出的聊客不存在", "消息", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ClientHandler handler = userManager.getHandler(nickname);
        if (handler == null) {
            JOptionPane.showMessageDialog(gui, "要踢出的聊客不存在", "消息", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // 发送被踢通知
        handler.sendMessage(MessageType.KICK, "您已被管理员踢出聊天室");
        handler.setKicked(true);
        handler.close();
        userManager.removeUser(nickname);
        gui.updateUserList(userManager.getAllNicknames());

        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String sysMsgContent = String.format("【%s,%s】：【因违规被踢出群聊室】", timestamp, nickname);
        String sysMsg = MessageUtil.encode(MessageType.SYS, sysMsgContent);
        patrolThread.addMessage(sysMsg);
        gui.clearKickField();

        // 成功踢出后弹窗
        JOptionPane.showMessageDialog(gui, "违规聊客已踢出", "消息", JOptionPane.INFORMATION_MESSAGE);
    }

    public void sendAdminMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String formattedMsg = String.format("【%s,管理员】：%s", timestamp, content);
        String encodedMsg = MessageUtil.encode(MessageType.SYS, formattedMsg);
        patrolThread.addMessage(encodedMsg);
        gui.clearInputField();
    }
}