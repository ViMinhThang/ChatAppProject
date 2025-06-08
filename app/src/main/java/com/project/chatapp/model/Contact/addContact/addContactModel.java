package com.project.chatapp.model.Contact.addContact;

import android.os.Parcel;
import android.os.Parcelable;

public class addContactModel implements Parcelable {
    private String name;
    private String status;
    private String phone;
    private String profile_picture; // chỉnh thành avatar user

    public addContactModel(String name, String phone, String status, String profile_picture) {
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.profile_picture = profile_picture;
    }

    // Constructor dùng để tạo từ Parcel
    protected addContactModel(Parcel in) {
        name = in.readString();
        status = in.readString();
        phone = in.readString();
        profile_picture = in.readString();
    }

    public static final Creator<addContactModel> CREATOR = new Creator<addContactModel>() {
        @Override
        public addContactModel createFromParcel(Parcel in) {
            return new addContactModel(in);
        }

        @Override
        public addContactModel[] newArray(int size) {
            return new addContactModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfile_picture() {
        return profile_picture;
    }

    @Override
    public int describeContents() {
        return 0; // không có file descriptor
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(phone);
        dest.writeString(profile_picture);
    }

    public void setStatus(String status) {
        this.status = status;
    }

}