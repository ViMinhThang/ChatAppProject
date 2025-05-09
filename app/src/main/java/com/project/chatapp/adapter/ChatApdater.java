package com.project.chatapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.ChatMessage;

import java.util.List;

public class ChatApdater extends RecyclerView.Adapter<ChatApdater.ChatViewHolder> {
    private List<ChatMessage> messageList;
    private String currentUserId;

    public ChatApdater(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.tvMessage.setText(message.getContent());
        holder.tvTime.setText(message.getTime());
    }

    public int getItemViewType(int position) {
        return messageList.get(position).isSender() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public ChatViewHolder(@NonNull View view) {
            super(view);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
