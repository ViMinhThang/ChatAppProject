package com.project.chatapp.screen.chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.model.CallModel;
import com.project.chatapp.screen.authentication.MainActivity;
import com.project.chatapp.services.NotificationService;
import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.screen.contact.ContactFragment;

import java.util.HashMap;
import java.util.Map;

public class ChatsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DatabaseReference mDatabase;
    private FirebaseMessengerRepository repo;
    private Map<String, Long> unreadCounts = new HashMap<>();
    private Map<String, String> chatNames = new HashMap<>();
    private static final String CHANNEL_ID = "ChatAppNotifications";
    private int notificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        repo = new FirebaseMessengerRepository();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ChatsFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_chats) {
                selectedFragment = new ChatsFragment();
            } else if (id == R.id.nav_contacts) {
                selectedFragment = new ContactFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new MoreFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        createNotificationChannel();
        startNotificationService();
        loadChatNames();
        listenForIncomingCall();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Chat Notifications";
            String description = "Notifications for new chat messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, NotificationService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d("ChatsActivity", "NotificationService started");
        } catch (Exception e) {
            Log.e("ChatsActivity", "Failed to start NotificationService", e);
        }
    }

    private void loadChatNames() {
        repo.getCurrentUserId(userId -> {
            if (userId == null) {
                Log.e("ChatsActivity", "Cannot load chat names - user ID is null");
                return;
            }

            DatabaseReference userChatsRef = mDatabase.child("users").child(userId).child("chats");
            userChatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        if (chatId != null) {
                            mDatabase.child("users").child(chatId).child("name")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String name = dataSnapshot.getValue(String.class);
                                            if (name != null) {
                                                chatNames.put(chatId, name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("ChatsActivity", "Failed to load user name: " + error.getMessage());
                                        }
                                    });

                            // Lấy số tin nhắn chưa đọc hiện tại
                            Long unreadCount = chatSnapshot.child("unread_count").getValue(Long.class);
                            if (unreadCount != null) {
                                unreadCounts.put(chatId, unreadCount);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatsActivity", "Failed to load chats: " + error.getMessage());
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset unread counts when returning to ChatsActivity
        repo.getCurrentUserId(userId -> {
            if (userId != null) {
                DatabaseReference userChatsRef = mDatabase.child("users").child(userId).child("chats");
                userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                            String chatId = chatSnapshot.getKey();
                            if (chatId != null) {
                                chatSnapshot.getRef().child("unread_count").setValue(0);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ChatsActivity", "Failed to reset unread counts: " + error.getMessage());
                    }
                });
            }
        });
    }

    private void listenForIncomingCall() {
        FirebaseMessengerRepository repo = new FirebaseMessengerRepository();
        repo.getCurrentUserId(myUserId -> {
            Log.d("CALL_DEBUG", "MainActivity lắng nghe userId: " + myUserId);
            if (myUserId == null) {
                Log.e("CALL_DEBUG", "userId null trong MainActivity!");
                return;
            }
            DatabaseReference callRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                    .child("calls").child(myUserId);
            callRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    Log.d("CALL_DEBUG", "onDataChange: " + snapshot.exists());
                    if (snapshot.exists()) {
                        CallModel call = snapshot.getValue(CallModel.class);
                        Log.d("CALL_DEBUG", "CallModel: from=" + (call != null ? call.from : "null") + ", to=" + (call != null ? call.to : "null") + ", myUserId=" + myUserId);
                        if (call != null && "audio".equals(call.type) && !call.from.equals(myUserId)) {
                            Intent intent = new Intent(ChatsActivity.this, com.project.chatapp.screen.chat.IncomingCallActivity.class);
                            intent.putExtra("callerName", call.callerName);
                            intent.putExtra("channelName", call.channelName);
                            intent.putExtra("fromUserId", call.from);
                            startActivity(intent);
                        } else {
                            Log.d("CALL_DEBUG", "Không mở IncomingCallActivity cho caller (myUserId=" + myUserId + ", from=" + (call != null ? call.from : "null") + ")");
                        }
                    }
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Log.e("CALL_DEBUG", "onCancelled: " + error.getMessage());
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không dừng NotificationService khi thoát khỏi ChatsActivity
        // để service tiếp tục lắng nghe tin nhắn mới ở background
    }
}