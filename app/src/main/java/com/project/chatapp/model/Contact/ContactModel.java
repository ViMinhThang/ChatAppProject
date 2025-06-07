package com.project.chatapp.model.Contact;

import java.io.Serializable;

public class ContactModel implements Serializable {
    private int img;
    private String name;
    private String phone;
    private String status;

    public ContactModel() {
    }

    public ContactModel(int img, String name, String phone, String status) {
        this.img = img;
        this.name = name;
        this.phone = phone;
        this.status = status;
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
}
