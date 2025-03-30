package com.project.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomAdapterRVChats extends RecyclerView.Adapter<CustomAdapterRVChats.ViewHolder> {
    private List<ChatsModel> listChats;

    public CustomAdapterRVChats(List<ChatsModel> listChats) {
        this.listChats = listChats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvchats, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatsModel chat = listChats.get(position);
        holder.img.setImageResource(chat.getImg());
        holder.name.setText(chat.getName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.time.setText(chat.getTime());
        holder.unread.setText(String.valueOf(chat.getUnread()));
    }

    @Override
    public int getItemCount() {
        return listChats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, lastMessage, time, unread;

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