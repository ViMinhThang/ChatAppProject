package com.project.chatapp.model.Contact;

public class ContactModel {
    private int img;
    private String name;
    private int phone;
    private String status;

    public ContactModel() {
    }

    public ContactModel(int img, String name, int phone, String status) {
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

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

}
