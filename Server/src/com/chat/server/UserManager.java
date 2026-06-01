package com.chat.server;

import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private ConcurrentHashMap<String, ClientHandler> users = new ConcurrentHashMap<>();

    public boolean addUser(String nickname, ClientHandler handler) {
        if (nickname == null || nickname.trim().isEmpty() || "管理员".equals(nickname)) {
            return false;
        }
        return users.putIfAbsent(nickname, handler) == null;
    }

    public void removeUser(String nickname) {
        users.remove(nickname);
    }

    public boolean isNicknameExist(String nickname) {
        return users.containsKey(nickname);
    }

    public ClientHandler getHandler(String nickname) {
        return users.get(nickname);
    }

    // 获取所有在线昵称（用于踢人界面等）
    public String[] getAllNicknames() {
        return users.keySet().toArray(new String[0]);
    }
}