package com.project.chatapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
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

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private List<Message> messageList;
    private Context context;
    private String searchQuery;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Message message, int position);
    }

    public SearchResultAdapter(Context context, List<Message> messageList, String searchQuery, OnItemClickListener listener) {
        this.context = context;
        this.messageList = messageList;
        this.searchQuery = searchQuery;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Hiển thị thông tin người gửi
        holder.tvSenderName.setText(message.getSenderName());

        // Hiển thị thời gian
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvMessageTime.setText(sdf.format(message.getTimestamp()));

        // Highlight từ khóa tìm kiếm trong nội dung tin nhắn
        String content = message.getContent();
        SpannableString spannableString = new SpannableString(content);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseContent = content.toLowerCase();
            String lowerCaseQuery = searchQuery.toLowerCase();

            int startPos = lowerCaseContent.indexOf(lowerCaseQuery);
            while (startPos >= 0) {
                int endPos = startPos + searchQuery.length();
                spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW),
                        startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                startPos = lowerCaseContent.indexOf(lowerCaseQuery, endPos);
            }
        }

        holder.tvMessageContent.setText(spannableString);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(message, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    public void updateSearchQuery(String query) {
        this.searchQuery = query;
        notifyDataSetChanged();
    }

    public void setMessages(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName;
        TextView tvMessageTime;
        TextView tvMessageContent;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
        }
    }
}
