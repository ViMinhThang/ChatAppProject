package com.project.chatapp.model.Contact;

public class ContactModel {
    private int img ;
    private String name ;
    private String status;
    public ContactModel (int img , String name , String status) {
        this.img = img ;
        this.name = name ;
        this.status = status ;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getAvatar() {
        return img;
    }
}
