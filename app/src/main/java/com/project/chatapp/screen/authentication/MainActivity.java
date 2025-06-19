package com.project.chatapp.screen.authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.services.NotificationService;
import com.project.chatapp.R;
import com.project.chatapp.model.CallModel;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.project.chatapp.screen.chat.ChatsActivity;
import com.project.chatapp.screen.chat.IncomingCallActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FrameLayout btnStartMessaging;
    private TextView tvTerms;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private static final int MAX_TOKEN_RETRIES = 3;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private int tokenRetryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Kiểm tra đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, ChatsActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "MainActivity onCreate chạy!");
        setContentView(R.layout.hello);

        if (checkPlayServices()) {
            initializeFirebase();
            initializeViews();
            checkNotificationPermission();
            listenForIncomingCall();
            getFCMToken();
        } else {
            Log.e(TAG, "Google Play Services không khả dụng");
            Toast.makeText(this, "Vui lòng cập nhật Google Play Services", 
                Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                    .show();
            } else {
                Log.e(TAG, "Thiết bị này không hỗ trợ Google Play Services.");
                Toast.makeText(this, "Thiết bị này không hỗ trợ Google Play Services", 
                    Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            
            // Kiểm tra kết nối Firebase
            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    Log.d(TAG, "Firebase connection state: " + (connected ? "connected" : "disconnected"));
                    if (!connected) {
                        Toast.makeText(MainActivity.this, 
                            "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng", 
                            Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Firebase connection listener cancelled", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Không thể khởi tạo Firebase: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        btnStartMessaging = findViewById(R.id.button_start);
        tvTerms = findViewById(R.id.terms_priva);

        btnStartMessaging.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, PhoneNumberActivity.class))
        );

        tvTerms.setOnClickListener(v -> {
            // TODO: Xử lý khi người dùng nhấn vào link Điều khoản & Chính sách
        });
    }

    private void getFCMToken() {
        if (tokenRetryCount >= MAX_TOKEN_RETRIES) {
            Log.e(TAG, "Đã thử lấy FCM token " + MAX_TOKEN_RETRIES + " lần, dừng thử lại");
            Toast.makeText(this, "Không thể lấy token thông báo. Một số tính năng có thể không hoạt động.", 
                Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra kết nối internet
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Không có kết nối internet");
            Toast.makeText(this, "Vui lòng kiểm tra kết nối mạng", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(TAG, "FCM token: " + token);
                    saveFCMToken(token);
                    tokenRetryCount = 0; // Reset counter on success
                } else {
                    Log.e(TAG, "Lấy FCM token thất bại", task.getException());
                    if (task.getException() instanceof IOException || 
                        (task.getException() != null && 
                         task.getException().getMessage() != null && 
                         task.getException().getMessage().contains("SERVICE_NOT_AVAILABLE"))) {
                        // Retry after delay if service not available
                        tokenRetryCount++;
                        new Handler().postDelayed(() -> {
                            if (!isFinishing()) {
                                getFCMToken();
                            }
                        }, 5000); // Retry after 5 seconds
                    }
                }
            });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = 
            (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.net.Network activeNetwork = connectivityManager.getActiveNetwork();
                if (activeNetwork != null) {
                    android.net.NetworkCapabilities networkCapabilities = 
                        connectivityManager.getNetworkCapabilities(activeNetwork);
                    return networkCapabilities != null && 
                        (networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) || 
                         networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
                }
            } else {
                android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void saveFCMToken(String token) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference tokenRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("fcmToken");
            
            tokenRef.setValue(token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retry getting token when activity resumes if we don't have one
        if (tokenRetryCount > 0 && tokenRetryCount < MAX_TOKEN_RETRIES) {
            getFCMToken();
        }
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                startNotificationService();
            }
        } else {
            startNotificationService();
        }
    }

    private void startNotificationService() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(TAG, "Khởi động NotificationService từ MainActivity");
            Intent serviceIntent = new Intent(this, NotificationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } else {
            Log.d(TAG, "Người dùng chưa đăng nhập, không khởi động service");
        }
    }

    private void listenForIncomingCall() {
        Log.d(TAG, "listenForIncomingCall() called");
        FirebaseMessengerRepository repo = new FirebaseMessengerRepository();
        repo.getCurrentUserId(myUserId -> {
            Log.d(TAG, "getCurrentUserId: " + myUserId);
            if (myUserId == null) {
                Log.e(TAG, "userId null trong MainActivity!");
                return;
            }
            DatabaseReference callRef = FirebaseDatabase.getInstance()
                    .getReference("calls")
                    .child(myUserId);
            callRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d(TAG, "Call data changed: " + snapshot.exists());
                    Log.d(TAG, "Snapshot value: " + snapshot.getValue());
                    if (snapshot.exists()) {
                        CallModel call = snapshot.getValue(CallModel.class);
                        Log.d(TAG, "CallModel: " + call);
                        if (call != null && "ringing".equals(call.getStatus()) && myUserId.equals(call.getReceiverId())) {
                            Log.d(TAG, "Có cuộc gọi đến, mở IncomingCallActivity");
                            Intent intent = new Intent(MainActivity.this, com.project.chatapp.screen.chat.IncomingCallActivity.class);
                            intent.putExtra("callId", call.getCallId());
                            intent.putExtra("callerId", call.getCallerId());
                            intent.putExtra("callerName", call.getCallerName());
                            intent.putExtra("type", call.getType());
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS permission granted");
                startNotificationService();
            } else {
                Log.d(TAG, "POST_NOTIFICATIONS permission denied");
                // TODO: Hiển thị dialog giải thích tại sao cần quyền này
            }
        }
    }
}