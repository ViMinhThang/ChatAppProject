package com.project.chatapp.model;

public class ChatMessage {
    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO
    }

    private String fromId;
    private String toId;
    private String content;
    private String timeStamp;
    private boolean isSender;
    private MessageType messageType;

    public ChatMessage(String fromId, String toId, String content, String timeStamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.timeStamp = timeStamp;
        this.messageType = detectMessageType(content);
    }

    private MessageType detectMessageType(String content) {
        if (content == null) return MessageType.TEXT;
        if (content.toLowerCase().contains("cloudinary.com")) {
            if (content.toLowerCase().contains("/video/")) {
                return MessageType.VIDEO;
            } else if (content.toLowerCase().contains("/image/")) {
                return MessageType.IMAGE;
            }
        }
        return MessageType.TEXT;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean isSender() {
        return isSender;
    }

    public String getContent() {
        return content;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public void setContent(String content) {
        this.content = content;
        this.messageType = detectMessageType(content);
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
