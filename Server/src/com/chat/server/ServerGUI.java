package com.chat.server;
import com.chat.protocol.ProtocolConst;
import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JTextField portField;
    private JButton startBtn;
    private JButton stopBtn;
    private JTextArea logArea;
    private JTextField kickField;
    private JButton kickBtn;
    private JTextField inputField;
    private  JButton sendBtn;

    private ServerController controller;  // 后续实现控制器

    public ServerGUI() {
        initUI();
        controller = new ServerController(this);
        setVisible(true);
    }

    private void initUI() {
        setTitle("全民聊天室 - 服务器端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portField = new JTextField(String.valueOf(ProtocolConst.DEFAULT_PORT), 10);
        topPanel.add(portField);
        startBtn = new JButton("启动服务");
        stopBtn = new JButton("关停服务");
        stopBtn.setEnabled(false);
        topPanel.add(startBtn);
        topPanel.add(stopBtn);

        kickBtn = new JButton("踢出一位聊客");
        kickBtn.setEnabled(false);
        topPanel.add(kickBtn);
        kickField = new JTextField(15);
        topPanel.add(kickField);
        add(topPanel, BorderLayout.NORTH);

        // 中间日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        //底部
        JPanel bottomPanel=new  JPanel(new FlowLayout(FlowLayout.LEFT));
        inputField=new JTextField(String.valueOf(ProtocolConst.DEFAULT_PORT));
        bottomPanel.add(inputField);
        sendBtn=new JButton("发送");
        bottomPanel.add(sendBtn);
        add(bottomPanel, BorderLayout.SOUTH);


        // 按钮事件（暂用匿名类，后续转移到 Controller）
        startBtn.addActionListener(e -> controller.startServer());
        stopBtn.addActionListener(e -> controller.stopServer());
        kickBtn.addActionListener(e -> controller.kickUser());

        pack();
    }

    // 供 Controller 调用的界面更新方法
    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public void setStartEnabled(boolean enabled) {
        startBtn.setEnabled(enabled);
        stopBtn.setEnabled(!enabled);
        portField.setEnabled(!enabled);
        kickBtn.setEnabled(!enabled);
    }

    public int getPort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String getKickNickname() {
        return kickField.getText().trim();
    }

    public void clearKickField() {
        kickField.setText("");
    }
}