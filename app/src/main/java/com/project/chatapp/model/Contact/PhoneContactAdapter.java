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

public class PhoneContactAdapter extends RecyclerView.Adapter<PhoneContactAdapter.ContactViewHolder> {
    
    private List<ContactModel> contactsList;
    private OnContactActionListener listener;
    
    public interface OnContactActionListener {
        void onAddFriend(ContactModel contact);
        void onCancelRequest(ContactModel contact);
        void onInvite(ContactModel contact);
    }
    
    public PhoneContactAdapter(List<ContactModel> contactsList) {
        this.contactsList = contactsList;
    }
    
    public void setOnContactActionListener(OnContactActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phone_contact, parent, false);
        return new ContactViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel contact = contactsList.get(position);
        
        holder.txtName.setText(contact.getName());
        holder.txtPhone.setText(contact.getPhone());
        
        // Load avatar
        if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(contact.getAvatarUrl())
                    .placeholder(R.drawable.user_info)
                    .error(R.drawable.user_info)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.user_info);
        }
        
        // Set button based on friend status
        String friendStatus = contact.getFriendStatus();
        if ("Đã là bạn".equals(friendStatus)) {
            holder.btnAction.setText("Đã là bạn");
            holder.btnAction.setEnabled(false);
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_disabled);
        } else if ("Đã gửi lời mời".equals(friendStatus)) {
            holder.btnAction.setText("Hủy lời mời");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_secondary);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelRequest(contact);
                }
            });
        } else if ("Kết bạn".equals(friendStatus)) {
            holder.btnAction.setText("Kết bạn");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_primary);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddFriend(contact);
                }
            });
        } else {
            // For contacts not using the app
            holder.btnAction.setText("Mời dùng app");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_secondary);
            holder.btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInvite(contact);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return contactsList.size();
    }
    
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName;
        TextView txtPhone;
        Button btnAction;
        
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
