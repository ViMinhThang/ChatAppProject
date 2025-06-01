package com.project.chatapp.model.Chat;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String content;
    private long timestamp;
    private String conversationId;
    private boolean isRead;

    public Message() {
    }

    public Message(String id, String senderId, String senderName, String content, long timestamp, String conversationId) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.conversationId = conversationId;
        this.isRead = false;
    }

    // Getters and setters
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getFormattedTime() {
        return com.project.chatapp.utils.TimeUtils.getTimeAgo(String.valueOf(timestamp));
    }

    public boolean containsKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty() || content == null) {
            return false;
        }
        return content.toLowerCase().contains(keyword.toLowerCase());
    }
}
