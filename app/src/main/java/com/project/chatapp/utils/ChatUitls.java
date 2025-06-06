package com.project.chatapp.utils;

public class ChatUitls {
    public static String getChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }
}
