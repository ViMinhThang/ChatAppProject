package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.data.ChatsRepository;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.databinding.FragmentChatsBinding;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.Chat.CustomAdapterRVChats;
import com.project.chatapp.model.Story.CustomAdapterRVStory;
import com.project.chatapp.model.Story.StoryModel;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private FragmentChatsBinding binding;
    private List<ChatsModel> listChats;
    private List<StoryModel> listStory;
    private CustomAdapterRVStory adapterStory;
    private CustomAdapterRVChats adapterChat;
    private ChatsRepository repo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate đúng binding fragment
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listStory = new ArrayList<>();
        listChats = new ArrayList<>();
        adapterStory = new CustomAdapterRVStory(listStory);
        repo = new ChatsRepository();

        binding.rvStory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvStory.setAdapter(adapterStory);

        new FirebaseMessengerRepository().getCurrentUserId(userId -> {
            adapterChat = new CustomAdapterRVChats(listChats, userId); // cần truyền thêm context nếu sửa adapter
            binding.rvChats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvChats.setAdapter(adapterChat);
            adapterChat.setOnChatClickListener(userId2 -> {
                Intent intent = new Intent(getContext(), MessageActivity.class);
                intent.putExtra("toUserId", userId2);
                startActivity(intent);
            });
            repo.loadUserChats(
                    chats -> {
                        listChats.clear();
                        listChats.addAll(chats);
                        adapterChat.notifyDataSetChanged();
                    },
                    stories -> {
                        listStory.clear();
                        listStory.addAll(stories);
                        adapterStory.notifyDataSetChanged();
                    }
            );
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}