package com.project.chatapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.Story.StoryModel;
import com.project.chatapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatsRepository {
    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public interface ChatsCallback {
        void onChatsLoaded(List<ChatsModel> chatList);
    }

    public interface UserIdCallback {
        void onUserIdFound(String userId);

        void onUserIdNotFound();
    }

    public interface StoryCallback {
        void onStoriesLoaded(List<StoryModel> storyList);
    }

    public interface UserNameCallback {
        void onUserNameLoaded(String name);
    }

    public ChatsRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void getUserIdByPhone(String phoneNumber, UserIdCallback callback) {
        Query query = mDatabase.child("users").orderByChild("phone").equalTo(phoneNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        callback.onUserIdFound(userSnapshot.getKey());
                        return;
                    }
                }
                callback.onUserIdNotFound();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("getUserIdByPhone", error.getMessage());
                callback.onUserIdNotFound();
            }
        });
    }

    public void loadUserChats(ChatsCallback chatsCallback, StoryCallback storyCallback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userPhoneNumber = currentUser.getPhoneNumber();
        Query query = mDatabase.child("users").orderByChild("phone").equalTo(userPhoneNumber);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ChatsModel> chats = new ArrayList<>();
                List<StoryModel> stories = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    // Load story (friends)
                    Map<String, Object> friendsMap = (Map<String, Object>) userSnapshot.child("friends").getValue();
                    if (friendsMap != null) {
                        for (Map.Entry<String, Object> entry : friendsMap.entrySet()) {
                            Map<String, Object> friendData = (Map<String, Object>) entry.getValue();
                            String friendName = (String) friendData.get("name");
                            stories.add(new StoryModel(R.drawable.common_google_signin_btn_icon_dark, friendName));
                        }
                    }

                    // Load chats
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

                                chats.add(new ChatsModel(R.drawable.common_google_signin_btn_icon_dark_normal, status, name, lastMessage, timeAgo, unreadCount, phone, chatId));
                                chatsCallback.onChatsLoaded(new ArrayList<>(chats));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("ChatUserError", error.getMessage());
                            }
                        });
                    }
                }

                storyCallback.onStoriesLoaded(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Query Error", error.getMessage());
            }
        });
    }

    public void getUserNameById(String userId, UserNameCallback callback) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                callback.onUserNameLoaded(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserNameLoaded(null);
            }
        });
    }
}
