package com.project.chatapp.model;

public class OptionModel {
    private int iconResId;
    private String name;

    public OptionModel(int iconResId, String name) {
        this.iconResId = iconResId;
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getName() {
        return name;
    }
}
