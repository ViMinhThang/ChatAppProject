package com.project.chatapp.model.Chat;

import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private Date timestamp;

    public Message() {
        // Empty constructor needed for Firebase
    }

    public Message(String id, String senderId, String senderName, String content, Date timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
