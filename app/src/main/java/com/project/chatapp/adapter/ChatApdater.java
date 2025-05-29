package com.project.chatapp.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.chatapp.R;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.screen.chat.ImageViewerActivity;

import java.util.List;

public class ChatApdater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_VIDEO = 3;

    private List<ChatMessage> messageList;

    public ChatApdater(List<ChatMessage> messageList) {
        this.messageList = messageList;
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
            bindTextMessage((TextViewHolder) holder, message);
        } else if (holder instanceof ImageViewHolder) {
            bindImageMessage((ImageViewHolder) holder, message);
        } else if (holder instanceof VideoViewHolder) {
            bindVideoMessage((VideoViewHolder) holder, message);
        }
    }

    private void bindTextMessage(TextViewHolder holder, ChatMessage message) {
        holder.tvMessage.setText(message.getContent());
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, holder.tvMessage, message.isSender());
    }

    private void bindImageMessage(ImageViewHolder holder, ChatMessage message) {
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
            .load(message.getContent())
            .into(holder.ivImage);
        holder.progressBar.setVisibility(View.GONE);
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        holder.ivImage.setOnClickListener(v -> {
            try {
                String imageUrl = message.getContent();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                    intent.putExtra("image_url", imageUrl);
                    holder.itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Invalid image URL", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(holder.itemView.getContext(), 
                    "Error opening image viewer: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindVideoMessage(VideoViewHolder holder, ChatMessage message) {
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.videoView.setVideoURI(Uri.parse(message.getContent()));
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        holder.videoView.setOnPreparedListener(mp -> {
            holder.progressBar.setVisibility(View.GONE);
            mp.setLooping(false);
            // Set video thumbnail
            mp.seekTo(100);
        });

        holder.videoView.setOnErrorListener((mp, what, extra) -> {
            holder.progressBar.setVisibility(View.GONE);
            Toast.makeText(holder.itemView.getContext(), "Error loading video", Toast.LENGTH_SHORT).show();
            return true;
        });

        holder.ivPlayButton.setOnClickListener(v -> {
            if (holder.videoView.isPlaying()) {
                holder.videoView.pause();
                holder.ivPlayButton.setVisibility(View.VISIBLE);
            } else {
                holder.videoView.start();
                holder.ivPlayButton.setVisibility(View.GONE);
            }
        });

        holder.videoView.setOnCompletionListener(mp -> {
            holder.ivPlayButton.setVisibility(View.VISIBLE);
        });
    }

    private void setMessageAlignment(LinearLayout container, TextView messageView, boolean isSender) {
        container.setGravity(isSender ? Gravity.END : Gravity.START);
        if (messageView != null) {
            messageView.setBackgroundResource(isSender ? R.drawable.bubble_right : R.drawable.buble_left);
            messageView.setTextColor(isSender ? Color.WHITE : Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
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
        VideoView videoView;
        ImageView ivPlayButton;
        TextView tvTime;
        ProgressBar progressBar;
        LinearLayout messageContainer;

        VideoViewHolder(@NonNull View view) {
            super(view);
            videoView = view.findViewById(R.id.videoView);
            ivPlayButton = view.findViewById(R.id.ivPlayButton);
            tvTime = view.findViewById(R.id.tvTime);
            progressBar = view.findViewById(R.id.progressBar);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }
}
