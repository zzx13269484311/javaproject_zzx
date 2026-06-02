package com.chat.client;

import javax.swing.*;
import java.awt.*;
import com.chat.protocol.ProtocolConst;

public class ClientGUI extends JFrame {
    private JTextField ipField;
    private JTextField portField;
    private JTextField nicknameField;
    private JButton enterBtn;
    private JButton exitBtn;
    private JTextArea chatArea;
    private JTextField msgField;
    private JButton sendBtn;

    private ClientController controller;

    public ClientGUI() {
        initUI();
        controller = new ClientController(this);
        setVisible(true);
    }

    private void initUI() {
        setTitle("全民大讨论聊天室 - 客户端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 顶部登录面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setPreferredSize(new Dimension(200, 70));
        topPanel.add(new JLabel("服务器IP:"));
        ipField = new JTextField("localhost");
        ipField.setPreferredSize(new Dimension(100, 25));
        topPanel.add(ipField);
        topPanel.add(new JLabel("服务器端口:"));
        portField = new JTextField(String.valueOf(ProtocolConst.DEFAULT_PORT), 6);
        portField.setPreferredSize(new Dimension(125, 25));
        topPanel.add(portField);
        topPanel.add(new JLabel("昵称:"));
        nicknameField = new JTextField();
        nicknameField.setPreferredSize(new Dimension(120, 25));
        topPanel.add(nicknameField);
        enterBtn = new JButton("进入聊天室");
        exitBtn = new JButton("退出聊天室");
        enterBtn.setPreferredSize(new Dimension(225, 25));
        exitBtn.setPreferredSize(new Dimension(225, 25));
        exitBtn.setEnabled(false);
        topPanel.add(enterBtn);
        topPanel.add(exitBtn,BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 中间聊天区域
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(chatArea);
        add(scroll, BorderLayout.CENTER);

        // 底部发送面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        msgField = new JTextField();
        sendBtn = new JButton("发送");
        sendBtn.setEnabled(false);
        msgField.setPreferredSize(new Dimension(700, 100));
        sendBtn.setPreferredSize(new Dimension(100, 100));
        bottomPanel.add(msgField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // 按钮事件
        enterBtn.addActionListener(e -> controller.login());
        exitBtn.addActionListener(e -> controller.logout());
        sendBtn.addActionListener(e -> controller.sendMessage(msgField.getText().trim()));
    }

    public void appendChat(String msg) {
        SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
    }

    public void setLoggedIn(boolean loggedIn) {
        SwingUtilities.invokeLater(() -> {
            enterBtn.setEnabled(!loggedIn);
            exitBtn.setEnabled(loggedIn);
            sendBtn.setEnabled(loggedIn);
            ipField.setEnabled(!loggedIn);
            portField.setEnabled(!loggedIn);
            nicknameField.setEnabled(!loggedIn);
        });
    }

    public String getServerIp() { return ipField.getText().trim(); }
    public int getPort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    public String getNickname() { return nicknameField.getText().trim(); }
    public void clearMsgField() { msgField.setText(""); }
}