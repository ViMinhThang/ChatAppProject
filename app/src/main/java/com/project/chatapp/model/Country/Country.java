package com.project.chatapp.model.Country;

public class Country {
    private String name;
    private String code;
    private int flagResId;

    public Country(String name, String code, int flagResId) {
        this.name = name;
        this.code = code;
        this.flagResId = flagResId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getFlagResId() {
        return flagResId;
    }
}