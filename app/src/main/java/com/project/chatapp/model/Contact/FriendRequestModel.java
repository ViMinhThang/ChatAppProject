package com.project.chatapp.model.Contact;

public class FriendRequestModel {
    private String userId;
    private String name;
    private String avatarUrl;
    private String status;
    private String timestamp;

    public FriendRequestModel() {
    }

    public FriendRequestModel(String userId, String name, String avatarUrl, String status, String timestamp) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
