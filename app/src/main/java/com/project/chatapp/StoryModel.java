package com.project.chatapp;

public class StoryModel {
    private int imgStory ;
    private String name ;
    public StoryModel (int imgStory , String name) {
        this.imgStory = imgStory ;
        this.name = name;

    }
    public int getImgStory () {
        return imgStory ;
    }
    public String getName () {
        return name;
    }
    public void setImgStory (int imgStory) {
        this.imgStory = imgStory ;
    }
    public void setName (String name) {
        this.name = name ;
}}
