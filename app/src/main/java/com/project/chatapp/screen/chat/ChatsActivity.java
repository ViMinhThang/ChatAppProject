package com.project.chatapp.screen.chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;

import java.util.HashMap;
import java.util.Map;

public class ChatsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DatabaseReference mDatabase;
    private FirebaseMessengerRepository repo;
    private Map<String, Long> unreadCounts = new HashMap<>();
    private Map<String, String> chatNames = new HashMap<>();
    private static final String CHANNEL_ID = "chat_notifications";
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
        setupChatsListener();
        loadChatNames(); // Giả định phương thức này được gọi từ ChatsFragment hoặc nơi khác
    }

    private void setupChatsListener() {
        repo.getCurrentUserId(myUserId -> {
            if (myUserId == null) {
                Log.e("ChatsActivity", "User ID is null");
                return;
            }

            DatabaseReference chatsRef = mDatabase.child("users").child(myUserId).child("chats");
            chatsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey();
                        Long newUnreadCount = chatSnapshot.child("unread_count").getValue(Long.class);
                        String lastContent = chatSnapshot.child("last_content").getValue(String.class);

                        if (newUnreadCount != null && lastContent != null && !chatId.startsWith("group")) {
                            Long prevUnreadCount = unreadCounts.getOrDefault(chatId, 0L);
                            if (newUnreadCount > prevUnreadCount) {
                                String senderName = chatNames.get(chatId);
                                if (senderName != null) {
                                    showNotification(senderName, lastContent, chatId);
                                }
                            }
                            unreadCounts.put(chatId, newUnreadCount);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatsListener", "Error: " + error.getMessage());
                }
            });
        });
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

    private void showNotification(String title, String message, String chatId) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("userId", chatId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Thay bằng icon của bạn
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId++, builder.build());
    }

    // Giả định phương thức này được gọi để tải tên người dùng
    private void loadChatNames() {
        // Cần triển khai logic để điền chatNames từ ChatsFragment hoặc ChatsRepository
        // Ví dụ: chatNames.put("user2", "Jane Smith");
    }
}