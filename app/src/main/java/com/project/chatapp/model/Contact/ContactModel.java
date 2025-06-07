package com.project.chatapp.model.Contact;

public class ContactModel {
    private String name ;
    private String status;
    private String profile_picture ; //chỉnh thành avatar user
    public ContactModel (String name , String status, String profile_picture) {
        this.name = name ;
        this.status = status ;
        this.profile_picture= profile_picture;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getProfile_picture() {
        return profile_picture;
    }
}
