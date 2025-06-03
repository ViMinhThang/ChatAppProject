package com.project.chatapp.model.Chat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.data.ChatsRepository;
import com.project.chatapp.screen.chat.MessageActivity;

import java.util.List;

public class CustomAdapterRVChats extends RecyclerView.Adapter<CustomAdapterRVChats.ViewHolder> {
    private List<ChatsModel> listChats;
    private String currentUserId;

    public CustomAdapterRVChats(List<ChatsModel> listChats, String currentUserId) {
        this.listChats = listChats;
        this.currentUserId = currentUserId;
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

        String lastMsg = chat.getLastMessage();
        String preview;
        if (lastMsg == null) {
            preview = "";
        } else if (isImageMessage(lastMsg)) {
            preview = isSentByMe(lastMsg) ? "Bạn đã gửi một ảnh" : "Bạn đã nhận một ảnh";
        } else if (isVideoMessage(lastMsg)) {
            preview = isSentByMe(lastMsg) ? "Bạn đã gửi một video" : "Bạn đã nhận một video";
        } else {
            preview = lastMsg;
        }
        holder.lastMessage.setText(preview);
        holder.time.setText(chat.getTime());

        long unreadCount = chat.getUnread();
        if (unreadCount > 0) {
            holder.unread.setText(String.valueOf(unreadCount));
            holder.unread.setVisibility(View.VISIBLE);
        } else {
            holder.unread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            ChatsRepository repo = new ChatsRepository();
            repo.getUserIdByPhone(chat.getUserPhoneNumber(), new ChatsRepository.UserIdCallback() {
                @Override
                public void onUserIdFound(String userId) {
                    Intent intent = new Intent(holder.itemView.getContext(), MessageActivity.class);
                    intent.putExtra("userId", userId);
                    holder.itemView.getContext().startActivity(intent);
                }

                @Override
                public void onUserIdNotFound() {
                    Toast.makeText(holder.itemView.getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return listChats.size();
    }

    private boolean isImageMessage(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        return lower.contains("cloudinary.com") && (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.contains("/image/"));
    }

    private boolean isVideoMessage(String content) {
        if (content == null) return false;
        String lower = content.toLowerCase();
        return lower.contains("cloudinary.com") && (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".3gp") || lower.contains("/video/"));
    }

    private boolean isSentByMe(String lastMsg) {
        if (lastMsg == null) return false;
        if (lastMsg.contains(":")) {
            String[] parts = lastMsg.split(":", 2);
            return parts[0].equals(currentUserId);
        }
        return false;
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