package com.project.chatapp.screen.authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.project.chatapp.services.NotificationService;
import com.project.chatapp.R;
import com.project.chatapp.model.CallModel;
import com.google.firebase.database.DatabaseReference;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FrameLayout btnStartMessaging;
    TextView tvTerms;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nếu đã đăng nhập thì chuyển thẳng vào ChatsActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(this, com.project.chatapp.screen.chat.ChatsActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d("CALL_DEBUG", "MainActivity onCreate chạy!");
        setContentView(R.layout.hello);

        // Kiểm tra quyền thông báo
        checkNotificationPermission();

        FirebaseApp.initializeApp(this);
        if (FirebaseApp.getApps(this).size() == 0) {
            Log.d("FirebaseTest", "Firebase Not initialized");
        } else {
            Log.d("FirebaseTest", "Firebase Initialized Sucessfully");
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        btnStartMessaging = findViewById(R.id.button_start);
        tvTerms = findViewById(R.id.terms_priva);

        btnStartMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                startActivity(intent);
            }
        });

        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Xử lý khi người dùng nhấn vào link Điều khoản & Chính sách
            }
        });

        listenForIncomingCall();

        // Lấy FCM token và log ra logcat
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d("FCM_DEBUG", "FCM token: " + token);
            } else {
                Log.e("FCM_DEBUG", "Lấy FCM token thất bại: " + task.getException());
            }
        });
    }

    private void checkNotificationPermission() {
        // Kiểm tra quyền thông báo trên Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền nếu chưa có
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                // Đã có quyền, khởi động service
                startNotificationService();
            }
        } else {
            // Android 12 trở xuống không cần xin quyền riêng cho thông báo
            startNotificationService();
        }
    }

    private void startNotificationService() {
        // Khởi động service thông báo nếu người dùng đã xác thực
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d("NotificationService", "Khởi động NotificationService từ MainActivity");
            Intent serviceIntent = new Intent(this, NotificationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            Log.d("NotificationService", "Người dùng chưa đăng nhập, không khởi động service");
        }
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
                            Intent intent = new Intent(MainActivity.this, com.project.chatapp.screen.chat.IncomingCallActivity.class);
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "POST_NOTIFICATIONS permission granted");
                startNotificationService();
            } else {
                Log.d("Permission", "POST_NOTIFICATIONS permission denied");
                // Thông báo cho người dùng biết về việc thiếu quyền thông báo
                // Có thể hiển thị dialog giải thích tại sao cần quyền này
            }
        }
    }
}