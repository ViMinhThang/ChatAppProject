package com.project.chatapp.model;

import com.google.firebase.database.PropertyName;

public class ChatMessage {
    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        LOCATION  // thêm kiểu mới
    }


    private String id;
    private String fromId;
    private String toId;
    private String content;
    private String timeStamp;
    private boolean isSender;
    private MessageType messageType;
    @PropertyName("type")
    public void setType(String type) {
        try {
            this.messageType = MessageType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            this.messageType = MessageType.TEXT;
        }
    }

    @PropertyName("type")
    public String getType() {
        return messageType.name().toLowerCase();
    }
    public ChatMessage() {

    }

    public ChatMessage(String fromId, String toId, String content, String timeStamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.timeStamp = timeStamp;
        this.messageType = detectMessageType(content);

    }

    private MessageType detectMessageType(String content) {
        if (content == null) return MessageType.TEXT;

        String lowerContent = content.toLowerCase();

        if (lowerContent.contains("cloudinary.com")) {
            if (lowerContent.contains("/video/")) {
                return MessageType.VIDEO;
            } else if (lowerContent.contains("/image/")) {
                return MessageType.IMAGE;
            }
        } else if (lowerContent.contains("location")) {
            return MessageType.LOCATION;
        }

        return MessageType.TEXT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}