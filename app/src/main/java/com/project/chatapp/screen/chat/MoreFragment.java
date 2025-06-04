package com.project.chatapp.screen.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.R;
import com.project.chatapp.adapter.OptionAdapter;
import com.project.chatapp.databinding.FragmentMoreBinding;
import com.project.chatapp.model.OptionModel;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {
    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cài đặt RecyclerView
        List<OptionModel> optionList = getOptionList();
        OptionAdapter adapter = new OptionAdapter(optionList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private List<OptionModel> getOptionList() {
        List<OptionModel> list = new ArrayList<>();
        list.add(new OptionModel(R.drawable.account, "My Profile"));
        list.add(new OptionModel(R.drawable.chat_nav, "Settings"));
        list.add(new OptionModel(R.drawable.appearance, "Help & Support"));
        list.add(new OptionModel(R.drawable.notification, "Logout"));
        return list;
    }
}
