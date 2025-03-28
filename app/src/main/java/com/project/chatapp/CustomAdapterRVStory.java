package com.project.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CustomAdapterRVStory extends RecyclerView.Adapter<CustomAdapterRVStory.ViewHolder> {
    private List<StoryModel> listStory;

    public CustomAdapterRVStory(List<StoryModel> list) {
        this.listStory = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvstory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryModel story = listStory.get(position);
        holder.imgStory.setImageResource(story.getImgStory());
        holder.txtName.setText(story.getName());
    }

    @Override
    public int getItemCount() {
        return listStory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgStory;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStory = itemView.findViewById(R.id.imgStory);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
