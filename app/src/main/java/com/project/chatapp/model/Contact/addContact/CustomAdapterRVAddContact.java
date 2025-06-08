package com.project.chatapp.model.Contact.addContact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;

import java.io.File;
import java.util.List;

public class CustomAdapterRVAddContact extends RecyclerView.Adapter<CustomAdapterRVAddContact.ViewHolder> {
    private List<addContactModel> listContact;

    public CustomAdapterRVAddContact(List<addContactModel> listContact) {
        this.listContact = listContact;
    }

    private OnAddContactClickListener listener;

    public interface OnAddContactClickListener {
        void onAddContactClicked(String phone);
    }

    public CustomAdapterRVAddContact(List<addContactModel> listContact, OnAddContactClickListener listener) {
        this.listContact = listContact;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_add, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        addContactModel contact = listContact.get(position);
        holder.txtName.setText(contact.getName());
        holder.txtPhone.setText(contact.getPhone());
        holder.btnAddContact.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddContactClicked(contact.getPhone());
            }
        });
        String path = contact.getProfile_picture();

        if (path != null) {
            if (path.startsWith("http")) {
                Glide.with(holder.imgAvatar.getContext())
                        .load(path + "?timestamp=" + System.currentTimeMillis())
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.imgAvatar);
            } else {
                Glide.with(holder.imgAvatar.getContext())
                        .load(new File(path))
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .into(holder.imgAvatar);
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
        }
    }


    // Cập nhật khi tìm được
    public void updateData(List<addContactModel> newList) {
        this.listContact = newList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return listContact.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone;
        ImageView imgAvatar;
        ImageButton btnAddContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameContacts);
            txtPhone = itemView.findViewById(R.id.txtPhone);
            imgAvatar = itemView.findViewById(R.id.imgAvtContact);
            btnAddContact = itemView.findViewById(R.id.btnAddContact);
        }
    }

}