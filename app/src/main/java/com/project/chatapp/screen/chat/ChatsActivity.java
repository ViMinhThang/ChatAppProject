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
import com.project.chatapp.services.NotificationService;
import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.screen.contact.ContactFragment;
import com.project.chatapp.model.CallModel;

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
    private static final String TAG = "ChatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        repo = new FirebaseMessengerRepository();

        // Set tab mặc định là Chats
        bottomNavigationView.setSelectedItemId(R.id.nav_chats);

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

            Log.d(TAG, "NotificationService started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start NotificationService", e);
        }
    }

    private void loadChatNames() {
        repo.getCurrentUserId(userId -> {
            if (userId == null) {
                Log.e(TAG, "Cannot load chat names - user ID is null");
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
                                            Log.e(TAG, "Failed to load user name: " + error.getMessage());
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
                    Log.e(TAG, "Failed to load chats: " + error.getMessage());
                }
            });
        });
    }

    private void listenForIncomingCall() {
        repo.getCurrentUserId(myUserId -> {
            if (myUserId == null) {
                Log.e(TAG, "Cannot listen for calls - user ID is null");
                return;
            }

            DatabaseReference callRef = FirebaseDatabase.getInstance().getReference()
                    .child("calls").child(myUserId);

            callRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            CallModel call = snapshot.getValue(CallModel.class);
                            if (call != null) {
                                String callType = call.getType();
                                String callerId = call.getCallerId();
                                String callerName = call.getCallerName();
                                String callId = call.getCallId();

                                if (callType != null && callerId != null && callerName != null && callId != null && !callerId.equals(myUserId)) {
                                    Intent intent = new Intent(ChatsActivity.this, IncomingCallActivity.class);
                                    intent.putExtra("callerName", callerName);
                                    intent.putExtra("callId", callId);
                                    intent.putExtra("callerId", callerId);
                                    intent.putExtra("type", callType);
                                    startActivity(intent);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing incoming call data", e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Call listener cancelled: " + error.getMessage());
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
                        Log.e(TAG, "Failed to reset unread counts: " + error.getMessage());
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không dừng NotificationService khi thoát khỏi ChatsActivity
        // để service tiếp tục lắng nghe tin nhắn mới ở background
    }
}