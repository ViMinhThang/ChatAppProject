package com.project.chatapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomAdapterRVChats {
    private List<ChatsModel> listChats;

    public CustomAdapterRVChats(List<ChatsModel> listChats) {
        this.listChats = listChats;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img ;
        TextView name , lastMessage , time , unread;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgAvtChat);
            name = itemView.findViewById(R.id.txtNameChat);
            lastMessage = itemView.findViewById(R.id.txtlastChats);
            time = itemView.findViewById(R.id.txtTimeChat);
            unread = itemView.findViewById(R.id.txtUnread);
        }
    }
}
