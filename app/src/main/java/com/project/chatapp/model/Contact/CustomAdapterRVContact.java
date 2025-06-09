package com.project.chatapp.model.Contact;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.chatapp.R;
import com.project.chatapp.screen.chat.ChatsActivity;

import java.util.List;

public class CustomAdapterRVContact extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private List<ContactModel> listContact;
    private OnFriendRequestListener friendRequestListener;
    private OnContactClickListener contactClickListener;

    public interface OnFriendRequestListener {
        void onSendFriendRequest(ContactModel contact);
        void onCancelFriendRequest(ContactModel contact);
    }

    public interface OnContactClickListener {
        void onContactClick(ContactModel contact);
        void onCallClick(ContactModel contact);
        void onVideoCallClick(ContactModel contact);
        void onMessageClick(ContactModel contact);
    }

    public CustomAdapterRVContact(List<ContactModel> listContact) {
        this.listContact = listContact;
    }

    public void setOnFriendRequestListener(OnFriendRequestListener listener) {
        this.friendRequestListener = listener;
    }

    public void setOnContactClickListener(OnContactClickListener listener) {
        this.contactClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ContactModel contact = listContact.get(position);
        if ("header".equals(contact.getStatus())) {
            return TYPE_HEADER;
        }
        return TYPE_CONTACT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactModel contact = listContact.get(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.txtHeader.setText(contact.getName());
        } else if (holder instanceof ContactViewHolder) {
            ContactViewHolder contactHolder = (ContactViewHolder) holder;
            contactHolder.txtName.setText(contact.getName());
            contactHolder.txtStatus.setText(contact.getStatus());

            // Hiển thị avatar
            if (!contact.getAvatarUrl().isEmpty()) {
                Glide.with(contactHolder.itemView.getContext())
                        .load(contact.getAvatarUrl())
                        .placeholder(R.drawable.user_info)
                        .error(R.drawable.user_info)
                        .into(contactHolder.imgAvatar);
            } else {
                contactHolder.imgAvatar.setImageResource(R.drawable.user_info);
            }

            // Xử lý hiển thị nút dựa vào trạng thái bạn bè
            String friendStatus = contact.getFriendStatus();

            // Ẩn tất cả các nút trước
            contactHolder.btnCall.setVisibility(View.GONE);
            contactHolder.btnVideoCall.setVisibility(View.GONE);
            contactHolder.btnMessage.setVisibility(View.GONE);
            contactHolder.btnAddFriend.setVisibility(View.GONE);

            if ("Đã là bạn".equals(friendStatus)) {
                // Đã là bạn bè, hiển thị nút gọi và nhắn tin
                contactHolder.btnCall.setVisibility(View.VISIBLE);
                contactHolder.btnVideoCall.setVisibility(View.VISIBLE);
                contactHolder.btnMessage.setVisibility(View.VISIBLE);

                contactHolder.btnCall.setOnClickListener(v -> {
                    if (contactClickListener != null) {
                        contactClickListener.onCallClick(contact);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + contact.getPhone()));
                        v.getContext().startActivity(intent);
                    }
                });

                contactHolder.btnVideoCall.setOnClickListener(v -> {
                    if (contactClickListener != null) {
                        contactClickListener.onVideoCallClick(contact);
                    } else {
                        Toast.makeText(v.getContext(), "Gọi video cho " + contact.getName(), Toast.LENGTH_SHORT).show();
                    }
                });

                contactHolder.btnMessage.setOnClickListener(v -> {
                    if (contactClickListener != null) {
                        contactClickListener.onMessageClick(contact);
                    } else {
                        Intent intent = new Intent(v.getContext(), ChatsActivity.class);
                        intent.putExtra("userId", contact.getUserId());
                        intent.putExtra("userName", contact.getName());
                        v.getContext().startActivity(intent);
                    }
                });
            } else if ("Kết bạn".equals(friendStatus)) {
                // Chưa là bạn bè, hiển thị nút kết bạn
                contactHolder.btnAddFriend.setVisibility(View.VISIBLE);
                contactHolder.btnAddFriend.setText("Kết bạn");

                contactHolder.btnAddFriend.setOnClickListener(v -> {
                    if (friendRequestListener != null) {
                        friendRequestListener.onSendFriendRequest(contact);
                    }
                });
            } else if ("Đã gửi lời mời".equals(friendStatus)) {
                // Đã gửi lời mời kết bạn, hiển thị nút hủy
                contactHolder.btnAddFriend.setVisibility(View.VISIBLE);
                contactHolder.btnAddFriend.setText("Hủy lời mời");

                contactHolder.btnAddFriend.setOnClickListener(v -> {
                    if (friendRequestListener != null) {
                        friendRequestListener.onCancelFriendRequest(contact);
                    }
                });
            } else if ("Mời dùng app".equals(friendStatus)) {
                contactHolder.btnAddFriend.setVisibility(View.VISIBLE);
                contactHolder.btnAddFriend.setText("Mời dùng app");

                contactHolder.btnAddFriend.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + contact.getPhone()));
                    intent.putExtra("sms_body", "Hãy tham gia cùng tôi trên ứng dụng chat: https://play.google.com/store/apps/details?id=com.project.chatapp");
                    v.getContext().startActivity(intent);
                });
            }

            contactHolder.itemView.setOnClickListener(v -> {
                if (contactClickListener != null) {
                    contactClickListener.onContactClick(contact);
                } else if ("Đã là bạn".equals(friendStatus)) {
                    Toast.makeText(v.getContext(), "Xem thông tin " + contact.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listContact.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtStatus;
        ImageView imgAvatar, btnCall, btnVideoCall, btnMessage;
        Button btnAddFriend;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameContacts);
            txtStatus = itemView.findViewById(R.id.txtStatusContact);
            imgAvatar = itemView.findViewById(R.id.imgAvtContact);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnVideoCall = itemView.findViewById(R.id.btnVideoCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtHeader = itemView.findViewById(R.id.txtHeader);
        }
    }
}
