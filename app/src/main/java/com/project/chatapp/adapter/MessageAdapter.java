package com.project.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.Chat.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId = "user1";

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = sdf.format(message.getTimestamp());

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder viewHolder = (SentMessageViewHolder) holder;
            viewHolder.tvMessage.setText(message.getContent());
            viewHolder.tvTime.setText(time);
        } else {
            ReceivedMessageViewHolder viewHolder = (ReceivedMessageViewHolder) holder;
            viewHolder.tvMessage.setText(message.getContent());
            viewHolder.tvTime.setText(time);
            viewHolder.tvSenderName.setText(message.getSenderName());
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
