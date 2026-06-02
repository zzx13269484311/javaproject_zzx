package com.chat.client;

import java.io.*;
import java.net.Socket;
import com.chat.protocol.*;

public class ReceiveThread extends Thread {
    private Socket socket;
    private ClientGUI gui;
    private BufferedReader reader;

    public ReceiveThread(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while (!isInterrupted() && (line = reader.readLine()) != null) {
                String[] parts = MessageUtil.decode(line);
                if (parts.length < 2) continue;
                String type = parts[0];
                String content = parts[1];
                if (MessageType.SYS.equals(type) || MessageType.MSG.equals(type)) {
                    gui.appendChat(content);
                } else if (MessageType.KICK.equals(type)) {
                    gui.appendChat("系统通知：" + content);
                    gui.setLoggedIn(false);
                    break;
                }
            }
        } catch (IOException e) {
            if (!isInterrupted()) {
                gui.appendChat("与服务器断开连接");
            }
        } finally {
            try {
                if (reader != null) reader.close();
                if (socket != null) socket.close();
            } catch (IOException e) { /* ignore */ }
            gui.setLoggedIn(false);
        }
    }
}