package com.project.chatapp.model;

public class ChatMessage {


    private String fromId;
    private String toId;
    private String content;
    private String timeStamp;

    private boolean isSender;

    public ChatMessage(String fromId, String toId, String content, String timeStamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.isSender = isSender;
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
