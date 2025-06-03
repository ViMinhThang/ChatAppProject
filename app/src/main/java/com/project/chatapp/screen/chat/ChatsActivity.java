package com.project.chatapp.screen.chat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.data.ChatsRepository;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.databinding.ActivityChatsBinding;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.Chat.CustomAdapterRVChats;
import com.project.chatapp.model.Story.CustomAdapterRVStory;
import com.project.chatapp.model.Story.StoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private ActivityChatsBinding binding;
    private List<ChatsModel> listChats;
    private List<StoryModel> listStory;
    private CustomAdapterRVStory adapterStory;
    private CustomAdapterRVChats adapterChat;
    private ChatsRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listStory = new ArrayList<>();
        listChats = new ArrayList<>();
        adapterStory = new CustomAdapterRVStory(listStory);
        adapterChat = new CustomAdapterRVChats(listChats);
        repo = new ChatsRepository();

        binding.rvStory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvStory.setAdapter(adapterStory);

        new FirebaseMessengerRepository().getCurrentUserId(userId -> {
            CustomAdapterRVChats adapterChat = new CustomAdapterRVChats(listChats, userId);
            binding.rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            binding.rvChats.setAdapter(adapterChat);

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
}
