package com.project.chatapp.model;

public class ChatMessage {
    private String content, time;
    boolean isSender;

    public ChatMessage(String senderId, String time, boolean message) {
        this.content = senderId;
        this.isSender = message;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public boolean isSender() {
        return isSender;
    }
}
