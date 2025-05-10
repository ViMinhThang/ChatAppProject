package com.project.chatapp.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.ChatMessage;

import java.util.List;

public class ChatApdater extends RecyclerView.Adapter<ChatApdater.ChatViewHolder> {
    private List<ChatMessage> messageList;

    public ChatApdater(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.tvMessage.setText(message.getContent());
        holder.tvTime.setText(message.getTime());
        LinearLayout container = holder.messageContainer;

        if (message.isSender()) {
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_right);
            holder.tvMessage.setTextColor(Color.WHITE);
            container.setGravity(Gravity.END);
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.buble_left);
            holder.tvMessage.setTextColor(Color.BLACK);
            container.setGravity(Gravity.START);
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        LinearLayout messageContainer;

        public ChatViewHolder(@NonNull View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }
}
