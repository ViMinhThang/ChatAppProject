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
import com.project.chatapp.utils.TimeUtils;

import org.checkerframework.checker.units.qual.C;

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
        listChats = new ArrayList<>();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userPhoneNumber = currentUser.getPhoneNumber();
        Query query = mDatabase.child("users").orderByChild("phone").equalTo(userPhoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                        Map<String, Object> friendsMap = (Map<String, Object>) userSnapshot.child("friends").getValue();
                        if (friendsMap != null) {
                            for (Map.Entry<String, Object> entry : friendsMap.entrySet()) {
                                Map<String, Object> friendData = (Map<String, Object>) entry.getValue();
                                String friendName = (String) friendData.get("name");
                                listStory.add(new StoryModel(R.drawable.pic1, friendName));
                            }
                            adapterStory.notifyDataSetChanged();
                        }

                        DataSnapshot chatsSnapshot = userSnapshot.child("chats");
                        for (DataSnapshot chatSnapshot : chatsSnapshot.getChildren()) {
                            String chatId = chatSnapshot.getKey();
                            if (chatId == null || chatId.startsWith("group")) continue;

                            String lastMessage = chatSnapshot.child("last_message").getValue(String.class);
                            String lastMessageTime = chatSnapshot.child("last_message_time").getValue(String.class);
                            Long unreadCount = chatSnapshot.child("unread_count").getValue(Long.class);

                            mDatabase.child("users").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot otherUserSnapshot) {
                                    String name = otherUserSnapshot.child("name").getValue(String.class);
                                    String status = otherUserSnapshot.child("status").getValue(String.class);
                                    String phone = otherUserSnapshot.child("phone").getValue(String.class);
                                    String timeAgo = TimeUtils.getTimeAgo(lastMessageTime);

                                    listChats.add(new ChatsModel(R.drawable.pic1, status, name, lastMessage, timeAgo, unreadCount, phone));
                                    adapterChat.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("ChatUserError", error.getMessage());
                                }
                            });
                        }
                    }
                } else {
                    Log.d("Query Result", "No user found with this phone number.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Query Error", error.getMessage());
            }
        });

        adapterStory = new CustomAdapterRVStory(listStory);
        binding.rvStory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvStory.setAdapter(adapterStory);

        adapterChat = new CustomAdapterRVChats(listChats);
        binding.rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.rvChats.setAdapter(adapterChat);
    }
}