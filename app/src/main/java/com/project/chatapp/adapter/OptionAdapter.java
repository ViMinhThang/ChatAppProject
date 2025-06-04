package com.project.chatapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.OptionModel;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {

    private final List<OptionModel> optionList;

    public OptionAdapter(List<OptionModel> optionList) {
        this.optionList = optionList;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_more_option, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        OptionModel option = optionList.get(position);
        holder.imageViewIcon.setImageResource(option.getIconResId());
        holder.textViewName.setText(option.getName());

        // Xử lý khi click vào mỗi mục (nếu cần)
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(),
                    "Clicked: " + option.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return optionList.size();
    }

    static class OptionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIcon;
        TextView textViewName;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewName = itemView.findViewById(R.id.textViewName);
        }
    }
}
