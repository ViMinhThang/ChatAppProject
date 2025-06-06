package com.project.chatapp.model;

public class CallModel {
    public String from;
    public String to;
    public String type;
    public String channelName;
    public String callerName;
    public long timestamp;

    public CallModel() {}

    public CallModel(String from, String to, String type, String channelName, String callerName, long timestamp) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.channelName = channelName;
        this.callerName = callerName;
        this.timestamp = timestamp;
    }
} 