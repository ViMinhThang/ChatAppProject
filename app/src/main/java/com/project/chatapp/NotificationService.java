package com.project.chatapp;

import android.app.AlarmManager;
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
import android.os.SystemClock;
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
            repo = new FirebaseMessengerRepository();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            // Tạo kênh thông báo ngay khi khởi động service
            createNotificationChannel();

            // Hiển thị thông báo foreground để dịch vụ không bị kill
            startForeground(SERVICE_ID, createForegroundNotification());

            // Thử lấy ID người dùng từ SharedPreferences trước
            SharedPreferences prefs = getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
            currentUserId = prefs.getString("currentUserId", null);

            // Nếu không có userId, thử lấy từ FirebaseAuth
            if (currentUserId == null) {
                repo.getCurrentUserId(userId -> {
                    if (userId != null) {
                        currentUserId = userId;
                        // Lưu vào SharedPreferences để dùng sau này
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
            Log.e("NotificationService", "Lỗi bảo mật: " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e("NotificationService", "Lỗi khởi tạo service: " + e.getMessage());
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
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String userId = chatSnapshot.getKey();
                    if (userId != null) {
                        loadUserName(userId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationService", "Lỗi khi tải danh sách chat: " + error.getMessage());
            }
        });
    }

    private void loadUserName(String userId) {
        mDatabase.child("users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            chatNames.put(userId, name);
                            Log.d("NotificationService", "Đã tải tên người dùng: " + userId + " = " + name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("NotificationService", "Lỗi khi tải tên người dùng: " + error.getMessage());
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
                    Long unreadCount = chatSnapshot.child("unread_count").getValue(Long.class);
                    String lastContent = chatSnapshot.child("last_content").getValue(String.class);

                    if (chatId != null && unreadCount != null && lastContent != null && unreadCount > 0) {
                        String fromName = chatNames.get(chatId);
                        if (fromName == null) {
                            // Nếu chưa có tên, tải tên ngay
                            loadUserName(chatId);
                            fromName = chatId;  // Tạm thời dùng ID
                        }
                        showNotification(chatId, fromName, lastContent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationService", "Lỗi khi lắng nghe tin nhắn mới: " + error.getMessage());
            }
        });
    }

    private void showNotification(String chatId, String fromName, String message) {
        if (chatId == null || message == null) {
            Log.e("NotificationService", "Không thể hiển thị thông báo vì dữ liệu null");
            return;
        }

        if (fromName == null) {
            fromName = "Người dùng";
        }

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("userId", chatId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                chatId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(fromName)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + chatId.hashCode(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Chat App")
                .setContentText("Đang lắng nghe tin nhắn mới")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("NotificationService", "Service bị hủy");
    }
}