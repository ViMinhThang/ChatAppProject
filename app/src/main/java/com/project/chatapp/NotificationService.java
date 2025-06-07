package com.project.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.screen.chat.MessageActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "ChatAppNotifications";
    private static final int SERVICE_ID = 1;
    private static final int NOTIFICATION_ID_BASE = 1000;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private FirebaseMessengerRepository repo;
    private Map<String, String> chatNames = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            createNotificationChannel();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            repo = new FirebaseMessengerRepository();

            // Tạo notification trước khi gọi startForeground
            Notification notification = getForegroundNotification();
            startForeground(SERVICE_ID, notification);

            // Lấy userId hiện tại từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE);
            currentUserId = prefs.getString("currentUserId", null);

            // Nếu không có userId, thử lấy từ FirebaseAuth
            if (currentUserId == null) {
                repo.getCurrentUserId(userId -> {
                    if (userId != null) {
                        currentUserId = userId;
                        // Lưu lại để lần sau dùng
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("currentUserId", userId);
                        editor.apply();

                        loadChatNames();
                        listenForNewMessages();
                    } else {
                        Log.e("NotificationService", "Không thể lấy userId từ FirebaseAuth");
                        stopSelf();
                    }
                });
            } else {
                loadChatNames();
                listenForNewMessages();
            }
        } catch (SecurityException e) {
            Log.e("NotificationService", "Không thể khởi động foreground service: " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e("NotificationService", "Lỗi khởi động service: " + e.getMessage());
            stopSelf();
        }
    }

    private void loadChatNames() {
        if (currentUserId == null) {
            Log.e("NotificationService", "currentUserId null, không thể tải tên chat");
            return;
        }

        DatabaseReference userChatsRef = mDatabase.child("users").child(currentUserId).child("chats");
        userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d("NotificationService", "Không có chat nào để tải tên");
                    return;
                }

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String userId = chatSnapshot.getKey();
                    if (userId != null) {
                        mDatabase.child("users").child(userId).child("name")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String name = snapshot.getValue(String.class);
                                        if (name != null) {
                                            chatNames.put(userId, name);
                                            Log.d("NotificationService", "Đã lấy tên: " + name + " cho userId: " + userId);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("NotificationService", "Lỗi khi lấy tên người dùng: " + error.getMessage());
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationService", "Lỗi khi tải tên chat: " + error.getMessage());
            }
        });
    }

    private void listenForNewMessages() {
        if (currentUserId == null) return;

        DatabaseReference chatsRef = mDatabase.child("users").child(currentUserId).child("chats");
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatId = chatSnapshot.getKey();
                    if (chatId == null) continue;

                    String lastContent = chatSnapshot.child("last_content").getValue(String.class);
                    String lastContentTime = chatSnapshot.child("last_content_time").getValue(String.class);
                    Long unreadCount = chatSnapshot.child("unread_count").getValue(Long.class);

                    if (lastContent == null || lastContentTime == null || unreadCount == null) continue;

                    SharedPreferences prefs = getSharedPreferences("ChatAppPrefs", MODE_PRIVATE);
                    String lastDisplayedTime = prefs.getString("last_displayed_time_" + chatId, "0");

                    if (Long.parseLong(lastContentTime) > Long.parseLong(lastDisplayedTime) && unreadCount > 0) {
                        // Người nhận tin nhắn
                        if (!chatId.equals(currentUserId)) {
                            String senderName = chatNames.getOrDefault(chatId, "Người dùng ChatApp");
                            showNotification(chatId, senderName, lastContent);

                            // Lưu thời gian hiển thị thông báo mới nhất
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("last_displayed_time_" + chatId, lastContentTime);
                            editor.apply();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationService", "Database error: " + error.getMessage());
            }
        });
    }

    private void showNotification(String chatId, String fromName, String message) {
        if (chatId == null || message == null) {
            Log.e("NotificationService", "Không thể hiển thị thông báo vì dữ liệu null");
            return;
        }

        // Đảm bảo fromName không null
        if (fromName == null) {
            fromName = "Người dùng ChatApp";
        }

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("userId", chatId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                chatId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_avatar_placeholder)
                    .setContentTitle(fromName)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID_BASE + chatId.hashCode(), builder.build());
        } catch (Exception e) {
            Log.e("NotificationService", "Lỗi hiển thị thông báo: " + e.getMessage());
        }
    }

    private Notification getForegroundNotification() {
        Intent notificationIntent = new Intent(this, MessageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ChatApp đang chạy")
                .setContentText("Đang lắng nghe tin nhắn mới")
                .setSmallIcon(R.drawable.ic_avatar_placeholder)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setDescription("Kênh thông báo cho tin nhắn mới");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("NotificationService", "Service đã được khởi động");
        return START_STICKY; // Khởi động lại service nếu bị kill
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NotificationService", "Service bị hủy");
    }
}