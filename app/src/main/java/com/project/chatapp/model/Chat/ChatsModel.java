package com.project.chatapp.model.Chat;

public class ChatsModel {
    private int img;
    private String name;
    private String lastMessage;
    private String time;
    private int unread;

    public ChatsModel(int img, boolean status, String name, String lastMessage, String time, int unread) {
        this.img = img;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.unread = unread;
    }

    public int getUnread() {
        return unread;
    }

    public String getTime() {
        return time;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getName() {
        return name;
    }


    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }
}
