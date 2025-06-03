package com.project.chatapp.model.Contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;

import java.util.List;

public class CustomAdapterRVContact extends RecyclerView.Adapter<CustomAdapterRVContact.ViewHolder> {
    private List<ContactModel> listContact;

    public CustomAdapterRVContact(List<ContactModel> listContact) {
        this.listContact = listContact;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvmessenger , parent , false) ;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contact = listContact.get(position);
        holder.txtName.setText(contact.getName());
        holder.txtStatus.setText(contact.getStatus());
        holder.imgAvatar.setImageResource(contact.getAvatar());
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
