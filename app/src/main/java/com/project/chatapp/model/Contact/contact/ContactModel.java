package com.project.chatapp.model.Contact.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactModel implements Parcelable {
    private String id;
    private String name;
    private String status;
    private String phone;
    private String profile_picture; // chỉnh thành avatar user

    public ContactModel(String id, String name, String phone, String status, String profile_picture) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.profile_picture = profile_picture;
    }

    // Constructor dùng để tạo từ Parcel
    protected ContactModel(Parcel in) {
        id= in.readString();
        name = in.readString();
        status = in.readString();
        phone = in.readString();
        profile_picture = in.readString();
    }

    public static final Creator<ContactModel> CREATOR = new Creator<ContactModel>() {
        @Override
        public ContactModel createFromParcel(Parcel in) {
            return new ContactModel(in);
        }

        @Override
        public ContactModel[] newArray(int size) {
            return new ContactModel[size];
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

    public String getId() {
        return id;
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(phone);
        dest.writeString(profile_picture);
    }

    public void setStatus(String status) {
        this.status = status;
    }

}