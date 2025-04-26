package com.project.chatapp.screen.chat;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.databinding.ActivityChatsBinding;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.Chat.CustomAdapterRVChats;
import com.project.chatapp.model.Story.CustomAdapterRVStory;
import com.project.chatapp.model.Story.StoryModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        listStory = new ArrayList<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userPhoneNumber = currentUser.getPhoneNumber();
        Query query = mDatabase.child("users").orderByChild("phone").equalTo(userPhoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        List<Map<String, Object>> friendsList = (List<Map<String, Object>>) userSnapshot.child("friends").getValue();
                        for (Map<String, Object> friend : friendsList) {
                            String friendName = (String) friend.get("name");
                            listStory.add(new StoryModel(R.drawable.pic1, friendName));
                        }
                    }
                } else {
                    Log.d("Query Result", "Không có user với phoneNumber này.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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