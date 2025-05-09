package com.project.chatapp.model;

public class SettingNav {
    private int imageResId;
    private String name;
    public SettingNav(int imageResId, String name){
        this.imageResId=imageResId;
        this.name=name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
