package com.project.chatapp.model.Contact;

import java.io.Serializable;

public class ContactModel implements Serializable {
    private int img;
    private String name;
    private String phone;
    private String status;
    private String friendStatus;
    private String userId;
    private String avatarUrl;

    public ContactModel() {
    }

    public ContactModel(int img, String name, String phone, String status) {
        this.img = img;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.friendStatus = "";
        this.userId = "";
        this.avatarUrl = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAvatar() {
        return img;
    }

    public void setAvatar(int img) {
        this.img = img;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(String friendStatus) {
        this.friendStatus = friendStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
