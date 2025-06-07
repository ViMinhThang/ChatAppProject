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

public class CustomAdapterRVContact extends RecyclerView.Adapter<CustomAdapterRVContact.ViewHolder> {

    private List<ContactModel> listContact;

    public CustomAdapterRVContact(List<ContactModel> listContact) {
        this.listContact = listContact;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactModel contact = listContact.get(position);
        holder.txtName.setText(contact.getName());
        holder.txtStatus.setText(contact.getStatus());
        holder.imgAvatar.setImageResource(contact.getAvatar());

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Selected " + contact.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contact.getPhone()));
            v.getContext().startActivity(intent);
        });

        holder.btnMessage.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Message " + contact.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listContact.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtStatus;
        ImageView imgAvatar, btnCall, btnMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtNameContacts);
            txtStatus = itemView.findViewById(R.id.txtStatusContact);
            imgAvatar = itemView.findViewById(R.id.imgAvtContact);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }
    }
}
