package com.project.chatapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.databinding.ActivityChatsBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity {
    private ActivityChatsBinding binding ;
    private List<StoryModel> listStory ;
    private CustomAdapterRVStory adapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatsBinding.inflate(getLayoutInflater()) ;
        setContentView(binding.getRoot());

        listStory = new ArrayList<>() ;
        listStory.add(new StoryModel(R.drawable.pic1,"tom")) ;
        listStory.add(new StoryModel(R.drawable.pic2,"meo")) ;
        listStory.add(new StoryModel(R.drawable.pic3,"cho")) ;

        adapter = new CustomAdapterRVStory(listStory);
        binding.rvStory.setLayoutManager(new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false));
        binding.rvStory.setAdapter(adapter);

    }
}
