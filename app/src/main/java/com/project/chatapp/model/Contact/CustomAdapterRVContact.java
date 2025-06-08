package com.project.chatapp.model.Contact;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;

import java.util.List;

public class CustomAdapterRVContact extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private List<ContactModel> listContact;

    public CustomAdapterRVContact(List<ContactModel> listContact) {
        this.listContact = listContact;
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

            if (contact.getAvatar() != 0) {
                contactHolder.imgAvatar.setImageResource(contact.getAvatar());
            }

            if ("Invite to app".equals(contact.getStatus())) {
                contactHolder.btnCall.setVisibility(View.GONE);
                contactHolder.btnMessage.setImageResource(android.R.drawable.ic_menu_send);
                contactHolder.btnMessage.setOnClickListener(v -> {
                    // Gửi SMS mời sử dụng app
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + contact.getPhone()));
                    intent.putExtra("sms_body", "Hey! Join me on ChatApp: https://play.google.com/store/apps/details?id=com.project.chatapp");
                    v.getContext().startActivity(intent);
                });
            } else {
                contactHolder.btnCall.setVisibility(View.VISIBLE);
                contactHolder.btnMessage.setImageResource(android.R.drawable.ic_dialog_email);

                contactHolder.btnCall.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contact.getPhone()));
                    v.getContext().startActivity(intent);
                });

                contactHolder.btnMessage.setOnClickListener(v -> {
                    Toast.makeText(v.getContext(), "Chat with " + contact.getName(), Toast.LENGTH_SHORT).show();
                });
            }

            contactHolder.itemView.setOnClickListener(v -> {
                if ("Invite to app".equals(contact.getStatus())) {
                    Toast.makeText(v.getContext(), "Invite " + contact.getName() + " to use the app", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "View profile of " + contact.getName(), Toast.LENGTH_SHORT).show();
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
        ImageView imgAvatar, btnCall, btnMessage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameContacts);
            txtStatus = itemView.findViewById(R.id.txtStatusContact);
            imgAvatar = itemView.findViewById(R.id.imgAvtContact);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
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
