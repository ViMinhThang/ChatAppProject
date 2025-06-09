package com.project.chatapp.model.Contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.chatapp.R;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    
    private List<FriendRequestModel> requestList;
    private OnFriendRequestActionListener listener;
    
    public interface OnFriendRequestActionListener {
        void onAcceptRequest(FriendRequestModel request);
        void onRejectRequest(FriendRequestModel request);
    }
    
    public FriendRequestAdapter(List<FriendRequestModel> requestList) {
        this.requestList = requestList;
    }
    
    public void setOnFriendRequestActionListener(OnFriendRequestActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestModel request = requestList.get(position);
        
        holder.txtName.setText(request.getName());
        holder.txtStatus.setText(request.getStatus());
        
        // Hiển thị avatar
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(request.getAvatarUrl())
                    .placeholder(R.drawable.user_info)
                    .error(R.drawable.user_info)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.user_info);
        }
        
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptRequest(request);
            }
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectRequest(request);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return requestList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName, txtStatus;
        Button btnAccept, btnReject;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
