package com.project.chatapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.project.chatapp.R;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.screen.chat.ImageViewerActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatApdater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_VIDEO = 3;

    private List<ChatMessage> messageList;
    private String searchQuery = "";
    private int highlightPosition = -1;
    private OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(int position);
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

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        switch (message.getMessageType()) {
            case IMAGE:
                return VIEW_TYPE_IMAGE;
            case VIDEO:
                return VIEW_TYPE_VIDEO;
            default:
                return VIEW_TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_IMAGE:
                return new ImageViewHolder(inflater.inflate(R.layout.item_chat_image, parent, false));
            case VIEW_TYPE_VIDEO:
                return new VideoViewHolder(inflater.inflate(R.layout.item_chat_video, parent, false));
            default:
                return new TextViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder instanceof TextViewHolder) {
            bindTextMessage((TextViewHolder) holder, message, position);
        } else if (holder instanceof ImageViewHolder) {
            bindImageMessage((ImageViewHolder) holder, message);
        } else if (holder instanceof VideoViewHolder) {
            bindVideoMessage((VideoViewHolder) holder, message);
        }
    }

    private void bindTextMessage(TextViewHolder holder, ChatMessage message, int position) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            holder.tvMessage.setText(highlightSearchText(message.getContent(), searchQuery));
        } else {
            holder.tvMessage.setText(message.getContent());
        }

        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, holder.tvMessage, message.isSender());

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
        SpannableString spannable = new SpannableString(text);
        Pattern pattern = Pattern.compile(Pattern.quote(searchQuery), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            spannable.setSpan(new BackgroundColorSpan(Color.YELLOW),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private void bindImageMessage(ImageViewHolder holder, ChatMessage message) {
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext()).load(message.getContent()).into(holder.ivImage);
        holder.progressBar.setVisibility(View.GONE);
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        holder.ivImage.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
            intent.putExtra("image_url", message.getContent());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private void bindVideoMessage(VideoViewHolder holder, ChatMessage message) {
        holder.progressBar.setVisibility(View.VISIBLE);
        if (holder.player != null) holder.player.release();

        holder.player = new ExoPlayer.Builder(holder.itemView.getContext()).build();
        holder.playerView.setPlayer(holder.player);
        holder.player.setMediaItem(MediaItem.fromUri(message.getContent()));
        holder.player.prepare();
        holder.progressBar.setVisibility(View.GONE);
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        holder.ivPlayButton.setOnClickListener(v -> {
            if (holder.player.isPlaying()) {
                holder.player.pause();
                holder.ivPlayButton.setVisibility(View.VISIBLE);
            } else {
                holder.player.play();
                holder.ivPlayButton.setVisibility(View.GONE);
            }
        });
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void setMessageAlignment(LinearLayout container, TextView msg, boolean isSender) {
        container.setGravity(isSender ? Gravity.END : Gravity.START);
        if (msg != null) {
            msg.setBackgroundResource(isSender ? R.drawable.bubble_right : R.drawable.buble_left);
            msg.setTextColor(isSender ? Color.WHITE : Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VideoViewHolder) {
            if (((VideoViewHolder) holder).player != null) {
                ((VideoViewHolder) holder).player.release();
                ((VideoViewHolder) holder).player = null;
            }
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        LinearLayout messageContainer;

        TextViewHolder(@NonNull View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTime;
        ProgressBar progressBar;
        LinearLayout messageContainer;

        ImageViewHolder(@NonNull View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivImage);
            tvTime = view.findViewById(R.id.tvTime);
            progressBar = view.findViewById(R.id.progressBar);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView playerView;
        ExoPlayer player;
        ImageView ivPlayButton;
        TextView tvTime;
        ProgressBar progressBar;
        LinearLayout messageContainer;

        VideoViewHolder(@NonNull View view) {
            super(view);
            playerView = view.findViewById(R.id.playerView);
            ivPlayButton = view.findViewById(R.id.ivPlayButton);
            tvTime = view.findViewById(R.id.tvTime);
            progressBar = view.findViewById(R.id.progressBar);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }
}
