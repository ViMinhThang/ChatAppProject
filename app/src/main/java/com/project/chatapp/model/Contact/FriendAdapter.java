package com.project.chatapp.model.Contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.chatapp.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    
    private List<ContactModel> friendsList;
    private OnFriendClickListener listener;
    
    public interface OnFriendClickListener {
        void onFriendClick(ContactModel friend);
        void onCallClick(ContactModel friend);
        void onVideoCallClick(ContactModel friend);
        void onMessageClick(ContactModel friend);
    }
    
    public FriendAdapter(List<ContactModel> friendsList) {
        this.friendsList = friendsList;
    }
    
    public void setOnFriendClickListener(OnFriendClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        ContactModel friend = friendsList.get(position);
        
        holder.txtName.setText(friend.getName());
        holder.txtStatus.setText(friend.getStatus());
        
        // Load avatar
        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.user_info)
                    .error(R.drawable.user_info)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.user_info);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend);
            }
        });
        
        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(friend);
            }
        });
        
        holder.btnVideoCall.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVideoCallClick(friend);
            }
        });
        
        holder.btnMessage.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(friend);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return friendsList.size();
    }
    
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName;
        TextView txtStatus;
        ImageView btnCall;
        ImageView btnVideoCall;
        ImageView btnMessage;
        
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnVideoCall = itemView.findViewById(R.id.btnVideoCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}
