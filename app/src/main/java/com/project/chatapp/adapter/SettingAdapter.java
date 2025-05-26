package com.project.chatapp.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.SettingNav;


import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {
    public List<SettingNav> settingNavList;

    public SettingAdapter(List<SettingNav> settingNavs) {
        this.settingNavList = settingNavs;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingNav item = settingNavList.get(position);
        holder.getNameTextView().setText(item.getName());
        holder.getNameImageView().setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return settingNavList.size();
    }

    public static class SettingViewHolder extends RecyclerView.ViewHolder {
        ImageView nameImageView;
        TextView nameTextView;

        public SettingViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewName);
            nameImageView = itemView.findViewById(R.id.imageViewIcon);
        }

        public ImageView getNameImageView() {
            return nameImageView;
        }

        public TextView getNameTextView() {
            return nameTextView;
        }

        public void setNameImageView(ImageView nameImageView) {
            this.nameImageView = nameImageView;
        }

        public void setNameTextView(TextView nameTextView) {
            this.nameTextView = nameTextView;
        }
    }
}
