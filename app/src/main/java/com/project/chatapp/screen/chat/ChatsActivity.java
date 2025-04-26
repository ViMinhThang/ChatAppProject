package com.project.chatapp.screen.chat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.R;
import com.project.chatapp.databinding.ActivityChatsBinding;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.Chat.CustomAdapterRVChats;
import com.project.chatapp.model.Story.CustomAdapterRVStory;
import com.project.chatapp.model.Story.StoryModel;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private ActivityChatsBinding binding;
    private List<ChatsModel> listChats;
    private List<StoryModel> listStory;
    private CustomAdapterRVStory adapterStory;
    private CustomAdapterRVChats adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listStory = new ArrayList<>();
        listStory.add(new StoryModel(R.drawable.pic1, "tom"));
        listStory.add(new StoryModel(R.drawable.pic2, "meo"));
        listStory.add(new StoryModel(R.drawable.pic3, "cho"));

        adapterStory = new CustomAdapterRVStory(listStory);
        binding.rvStory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvStory.setAdapter(adapterStory);

        listChats = new ArrayList<>();
        listChats.add(new ChatsModel(R.drawable.pic1, true, "My crush", "Em an com chua?", "now", 1));
        listChats.add(new ChatsModel(R.drawable.pic2, true, "Peter", "Em an com chua?", "tomorrow", 0));
        adapterChat = new CustomAdapterRVChats(listChats);
        binding.rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvChats.setAdapter(adapterChat);
    }
}