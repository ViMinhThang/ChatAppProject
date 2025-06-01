package com.project.chatapp.adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatApdater extends RecyclerView.Adapter<ChatApdater.ChatViewHolder> {
    private List<ChatMessage> messageList;
    private String searchQuery = "";
    private int highlightPosition = -1;
    private OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(int position);
    }

    public ChatApdater(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    public ChatApdater(List<ChatMessage> messageList, OnMessageClickListener clickListener) {
        this.messageList = messageList;
        this.clickListener = clickListener;
    }

    public void updateMessages(List<ChatMessage> messages, String searchQuery, int highlightPosition) {
        this.messageList = messages;
        this.searchQuery = searchQuery;
        this.highlightPosition = highlightPosition;
        notifyDataSetChanged();
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

        if (searchQuery != null && !searchQuery.isEmpty()) {
            holder.tvMessage.setText(highlightSearchText(message.getContent(), searchQuery));
        } else {
            holder.tvMessage.setText(message.getContent());
        }

        holder.tvTime.setText(formatTime(message.getTimeStamp()));

        LinearLayout container = holder.messageContainer;
        LinearLayout messageWrapper = (LinearLayout) holder.tvMessage.getParent();

        if (message.isSender()) {
            holder.tvMessage.setBackgroundResource(R.drawable.bubble_right);
            holder.tvMessage.setTextColor(Color.WHITE);
            container.setGravity(Gravity.END);
            if (messageWrapper != null) {
                messageWrapper.setGravity(Gravity.END);
            }
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.buble_left);
            holder.tvMessage.setTextColor(Color.BLACK);
            container.setGravity(Gravity.START);
            if (messageWrapper != null) {
                messageWrapper.setGravity(Gravity.START);
            }
        }

        if (position == highlightPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFE0B2"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onMessageClick(position);
            }
        });
    }

    private SpannableString highlightSearchText(String text, String searchQuery) {
        SpannableString spannableString = new SpannableString(text);

        if (searchQuery != null && !searchQuery.isEmpty()) {
            Pattern pattern = Pattern.compile(Pattern.quote(searchQuery), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                spannableString.setSpan(
                        new BackgroundColorSpan(Color.YELLOW),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return spannableString;
    }

    private String formatTime(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);

            if (timestamp.length() == 10) {
                time = time * 1000;
            }

            Date messageDate = new Date(time);
            Date now = new Date();

            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(messageDate);

            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(now);

            if (isSameDay(messageCal, nowCal)) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return timeFormat.format(messageDate);
            }

            nowCal.add(Calendar.DAY_OF_YEAR, -1);
            if (isSameDay(messageCal, nowCal)) {
                return "Hôm qua";
            }

            nowCal.setTime(now);
            long diffInMillis = now.getTime() - messageDate.getTime();
            long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

            if (diffInDays < 7) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                return dayFormat.format(messageDate);
            }

            if (messageCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                return dateFormat.format(messageDate);
            }

            SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return fullDateFormat.format(messageDate);

        } catch (NumberFormatException e) {
            Log.e("ChatAdapter", "Error parsing timestamp: " + timestamp, e);
            return ""; // Trả về chuỗi rỗng thay vì timestamp gốc
        } catch (Exception e) {
            Log.e("ChatAdapter", "Error formatting time", e);
            return "";
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
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
