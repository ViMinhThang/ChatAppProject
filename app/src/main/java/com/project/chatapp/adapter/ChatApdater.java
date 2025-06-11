package com.project.chatapp.adapter;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.project.chatapp.R;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.screen.chat.ImageViewerActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ChatApdater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_VIDEO = 3;
    private static final int VIEW_TYPE_LOCATION = 4;
    private static final int VIEW_TYPE_VOICE = 5;

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
            case VOICE:
                return VIEW_TYPE_VOICE;
            case IMAGE:
                return VIEW_TYPE_IMAGE;
            case VIDEO:
                return VIEW_TYPE_VIDEO;
            case LOCATION:
                return VIEW_TYPE_LOCATION;
            default:
                return VIEW_TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_VOICE:
                return new VoiceMessageViewHolder(
                        inflater.inflate(R.layout.item_chat_voice, parent, false)
                );
            case VIEW_TYPE_IMAGE:
                return new ImageViewHolder(
                        inflater.inflate(R.layout.item_chat_image, parent, false)
                );
            case VIEW_TYPE_VIDEO:
                return new VideoViewHolder(
                        inflater.inflate(R.layout.item_chat_video, parent, false)
                );
            case VIEW_TYPE_LOCATION:
                return new LocationViewHolder(
                        inflater.inflate(R.layout.item_chat_location, parent, false)
                );
            default:
                return new TextViewHolder(
                        inflater.inflate(R.layout.item_chat, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // Thêm long press listener cho tất cả các loại tin nhắn
        holder.itemView.setOnLongClickListener(v -> {
            if (!message.isDeletedForMe()) {
                showDeleteDialog(holder.itemView, message, position);
            }
            return true;
        });

        // Kiểm tra nếu tin nhắn đã bị xóa
        if (message.isDeletedForMe()) {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_TEXT:
                    TextViewHolder textHolder = (TextViewHolder) holder;
                    textHolder.tvMessage.setText("Tin nhắn đã bị xóa");
                    textHolder.tvMessage.setTextColor(Color.GRAY);
                    textHolder.tvMessage.setTypeface(null, Typeface.ITALIC);
                    break;
                case VIEW_TYPE_IMAGE:
                case VIEW_TYPE_VIDEO:
                case VIEW_TYPE_VOICE:
                case VIEW_TYPE_LOCATION:
                    hideMediaContent(holder);
                    break;
            }
            return;
        }

        // Xử lý tin nhắn bình thường
        switch (holder.getClass().getSimpleName()) {
            case "VoiceMessageViewHolder":
                bindVoiceMessage((VoiceMessageViewHolder) holder, message);
                break;
            case "TextViewHolder":
                bindTextMessage((TextViewHolder) holder, message, position);
                break;
            case "ImageViewHolder":
                bindImageMessage((ImageViewHolder) holder, message);
                break;
            case "VideoViewHolder":
                bindVideoMessage((VideoViewHolder) holder, message);
                break;
            case "LocationViewHolder":
                bindLocationMessage((LocationViewHolder) holder, message);
                break;
        }
    }

    private void showDeleteDialog(View anchorView, ChatMessage message, int position) {
        Context context = anchorView.getContext();
        PopupMenu popup = new PopupMenu(context, anchorView);
        popup.getMenu().add("Xóa tin nhắn");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Xóa tin nhắn")) {
                new AlertDialog.Builder(context)
                        .setTitle("Xóa tin nhắn")
                        .setMessage("Bạn có chắc chắn muốn xóa tin nhắn này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (deleteListener != null) {
                                deleteListener.onDeleteMessage(message, position);
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void hideMediaContent(RecyclerView.ViewHolder holder) {
        TextView deletedText = new TextView(holder.itemView.getContext());
        deletedText.setText("Tin nhắn đã bị xóa");
        deletedText.setTextColor(Color.GRAY);
        deletedText.setTypeface(null, Typeface.ITALIC);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_IMAGE:
                ImageViewHolder imageHolder = (ImageViewHolder) holder;
                imageHolder.ivImage.setVisibility(View.GONE);
                ((ViewGroup) imageHolder.ivImage.getParent()).addView(deletedText);
                break;
            case VIEW_TYPE_VIDEO:
                VideoViewHolder videoHolder = (VideoViewHolder) holder;
                videoHolder.playerView.setVisibility(View.GONE);
                ((ViewGroup) videoHolder.playerView.getParent()).addView(deletedText);
                break;
            case VIEW_TYPE_VOICE:
                VoiceMessageViewHolder voiceHolder = (VoiceMessageViewHolder) holder;
                voiceHolder.waveContainer.setVisibility(View.GONE);
                ((ViewGroup) voiceHolder.waveContainer.getParent()).addView(deletedText);
                break;
            case VIEW_TYPE_LOCATION:
                LocationViewHolder locationHolder = (LocationViewHolder) holder;
                locationHolder.tvLocation.setText("Tin nhắn đã bị xóa");
                locationHolder.tvLocation.setEnabled(false);
                locationHolder.tvLocation.setTextColor(Color.GRAY);
                locationHolder.tvLocation.setTypeface(null, Typeface.ITALIC);
                break;
        }
    }

    public interface OnDeleteMessageListener {
        void onDeleteMessage(ChatMessage message, int position);
    }

    private OnDeleteMessageListener deleteListener;

    public void setOnDeleteMessageListener(OnDeleteMessageListener listener) {
        this.deleteListener = listener;
    }
    private void bindVoiceMessage(VoiceMessageViewHolder holder, ChatMessage message) {
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        String voiceUrl = message.getContent().startsWith("voice:") ?
                message.getContent().substring(6) : message.getContent();

        holder.ivPlayPause.setOnClickListener(v -> {
            if (holder.isPlaying) {
                holder.pauseVoice();
            } else {
                holder.playVoice(voiceUrl);
            }
        });
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

    private void bindImageMessage(ImageViewHolder holder, ChatMessage message) {
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(holder.itemView.getContext())
                .load(message.getContent())
                .into(holder.ivImage);
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

    private void bindLocationMessage(LocationViewHolder holder, ChatMessage message) {
        holder.tvLocation.setText("Xem vị trí");
        holder.tvTime.setText(message.getTimeStamp());
        setMessageAlignment(holder.messageContainer, null, message.isSender());

        holder.tvLocation.setOnClickListener(v -> {
            double[] coordinates = message.getLocationCoordinates();
            if (coordinates != null) {
                double lat = coordinates[0];
                double lng = coordinates[1];

                String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Vị trí được chia sẻ)";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");

                if (intent.resolveActivity(holder.itemView.getContext().getPackageManager()) != null) {
                    holder.itemView.getContext().startActivity(intent);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Không tìm thấy ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(holder.itemView.getContext(), "Tọa độ không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private SpannableString highlightSearchText(String text, String searchQuery) {
        SpannableString spannable = new SpannableString(text);
        Pattern pattern = Pattern.compile(Pattern.quote(searchQuery), Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            spannable.setSpan(new BackgroundColorSpan(Color.YELLOW),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
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
            VideoViewHolder videoHolder = (VideoViewHolder) holder;
            if (videoHolder.player != null) {
                videoHolder.player.release();
                videoHolder.player = null;
            }
        } else if (holder instanceof VoiceMessageViewHolder) {
            VoiceMessageViewHolder voiceHolder = (VoiceMessageViewHolder) holder;
            voiceHolder.release();
        }
    }

    // ViewHolder Classes
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

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvTime;
        LinearLayout messageContainer;

        LocationViewHolder(@NonNull View view) {
            super(view);
            tvLocation = view.findViewById(R.id.tvLocation);
            tvTime = view.findViewById(R.id.tvTime);
            messageContainer = view.findViewById(R.id.messageContainer);
        }
    }

    static class VoiceMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlayPause;
        TextView tvDuration, tvTime;
        LinearLayout messageContainer;
        LinearLayout waveContainer;
        private MediaPlayer mediaPlayer;
        boolean isPlaying = false;
        private Handler handler = new Handler(Looper.getMainLooper());
        private ValueAnimator[] waveAnimators;

        VoiceMessageViewHolder(@NonNull View view) {
            super(view);
            ivPlayPause = view.findViewById(R.id.ivPlayPause);
            tvDuration = view.findViewById(R.id.tvDuration);
            tvTime = view.findViewById(R.id.tvTime);
            messageContainer = view.findViewById(R.id.messageContainer);
            waveContainer = view.findViewById(R.id.waveContainer);
        }

        void playVoice(String url) {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        isPlaying = true;
                        ivPlayPause.setImageResource(R.drawable.ic_pause);
                        tvDuration.setText(formatTime(mp.getDuration()));
                        animateWaveBars(true);
                    });
                    mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e("VoicePlayer", "Error playing voice message: " + what + ", " + extra);
                        Toast.makeText(itemView.getContext(), "Không thể phát tin nhắn thoại", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    Log.e("VoicePlayer", "Error setting data source: " + e.getMessage());
                    Toast.makeText(itemView.getContext(), "Không thể phát tin nhắn thoại", Toast.LENGTH_SHORT).show();
                }
            } else {
                mediaPlayer.start();
                isPlaying = true;
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                animateWaveBars(true);
            }
        }

        void pauseVoice() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                ivPlayPause.setImageResource(R.drawable.ic_play);
                animateWaveBars(false);
            }
        }

        private void stopPlayback() {
            isPlaying = false;
            ivPlayPause.setImageResource(R.drawable.ic_play);
            animateWaveBars(false);
        }

        void release() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                } catch (IllegalStateException e) {
                    Log.e("VoicePlayer", "Error releasing MediaPlayer: " + e.getMessage());
                }
                mediaPlayer = null;
            }
            animateWaveBars(false);
        }

        private void animateWaveBars(boolean isPlaying) {
            if (waveAnimators != null) {
                for (ValueAnimator animator : waveAnimators) {
                    if (animator != null) {
                        animator.cancel();
                    }
                }
            }

            if (isPlaying) {
                waveAnimators = new ValueAnimator[waveContainer.getChildCount()];
                for (int i = 0; i < waveContainer.getChildCount(); i++) {
                    View bar = waveContainer.getChildAt(i);
                    ValueAnimator animator = ValueAnimator.ofInt(6, 16);
                    animator.setDuration(600 + (i * 50));
                    animator.setRepeatCount(ValueAnimator.INFINITE);
                    animator.setRepeatMode(ValueAnimator.REVERSE);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());

                    int finalI = i;
                    animator.addUpdateListener(animation -> {
                        int value = (int) animation.getAnimatedValue();
                        ViewGroup.LayoutParams params = bar.getLayoutParams();
                        params.height = value + (finalI % 3) * 2;
                        bar.setLayoutParams(params);
                    });

                    animator.setStartDelay(i * 100);
                    animator.start();
                    waveAnimators[i] = animator;
                }
            } else {
                // Reset to default heights
                for (int i = 0; i < waveContainer.getChildCount(); i++) {
                    View bar = waveContainer.getChildAt(i);
                    ViewGroup.LayoutParams params = bar.getLayoutParams();
                    params.height = 8 + (i % 3) * 4;
                    bar.setLayoutParams(params);
                }
            }
        }

        private String formatTime(int milliseconds) {
            int seconds = (milliseconds / 1000) % 60;
            int minutes = (milliseconds / (1000 * 60)) % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
    }

}