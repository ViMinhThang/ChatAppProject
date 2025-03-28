package com.project.chatapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


import com.project.chatapp.databinding.ActivityChatsBinding;

public class ChatsActivity extends AppCompatActivity {
    private ActivityChatsBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatsBinding.inflate(getLayoutInflater()) ;
        setContentView(binding.getRoot());

    }
}