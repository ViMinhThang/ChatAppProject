package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;

public class OutgoingCallActivity extends AppCompatActivity {
    private String callId, currentUserId, remoteUserId;
    private DatabaseReference callRef;
    private TextView userNameText, callStatusText;
    private ImageView userAvatar;
    private ImageButton endCallButton;
    private static final String TAG = "OutgoingCallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);
        callId = getIntent().getStringExtra("callId");
        currentUserId = getIntent().getStringExtra("currentUserId");
        remoteUserId = getIntent().getStringExtra("remoteUserId");
        userNameText = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);
        callStatusText = findViewById(R.id.callStatusText);
        endCallButton = findViewById(R.id.endCallButton);
        callStatusText.setText("Đang gọi...");
        // Lấy tên đối phương
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(remoteUserId);
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null) userNameText.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        // Lắng nghe status node call
        callRef = FirebaseDatabase.getInstance().getReference().child("calls").child(remoteUserId);
        callRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    finishToChat();
                    return;
                }
                if (snapshot.child("status").exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("accepted".equals(status)) {
                        // Sang màn hình gọi thật sự
                        Intent intent = new Intent(OutgoingCallActivity.this, AudioCallActivity.class);
                        intent.putExtra("callId", callId);
                        intent.putExtra("currentUserId", currentUserId);
                        intent.putExtra("remoteUserId", remoteUserId);
                        intent.putExtra("isInitiator", true);
                        startActivity(intent);
                        finish();
                    } else if ("declined".equals(status)) {
                        Toast.makeText(OutgoingCallActivity.this, "Đối phương đã từ chối cuộc gọi", Toast.LENGTH_SHORT).show();
                        finishToChat();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        endCallButton.setOnClickListener(v -> endCall());
    }
    private void endCall() {
        // Xóa node call của cả 2 bên
        if (currentUserId != null) {
            FirebaseDatabase.getInstance().getReference().child("calls").child(currentUserId).removeValue();
        }
        if (remoteUserId != null) {
            FirebaseDatabase.getInstance().getReference().child("calls").child(remoteUserId).removeValue();
        }
        finishToChat();
    }
    private void finishToChat() {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("toUserId", remoteUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
} 