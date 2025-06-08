package com.project.chatapp.model.Contact.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project.chatapp.R;

import java.io.File;
import java.util.List;

public class CustomAdapterRVContact extends RecyclerView.Adapter<CustomAdapterRVContact.ViewHolder> {
    private List<ContactModel> listContact;

    public CustomAdapterRVContact(List<ContactModel> listContact) {
        this.listContact = listContact;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvmessenger, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contact = listContact.get(position);
        holder.txtName.setText(contact.getName());
        holder.txtStatus.setText(contact.getStatus());

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
    public void updateData(List<ContactModel> newList) {
        this.listContact = newList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return listContact.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtStatus;
        ImageView imgAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameContacts);
            txtStatus = itemView.findViewById(R.id.txtStatusContact);
            imgAvatar = itemView.findViewById(R.id.imgAvtContact);
        }
    }
}