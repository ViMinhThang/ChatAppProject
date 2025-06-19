package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.media.AudioAttributes;
import android.widget.ImageButton;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.project.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.data.FirebaseMessengerRepository;

public class IncomingCallActivity extends AppCompatActivity {
    private String callId, callerId, callerName, type, myUserId;
    private DatabaseReference callRef;
    private MediaPlayer mediaPlayer;
    private static final String TAG = "IncomingCallActivity";
    private ImageButton acceptButton, declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        callId = getIntent().getStringExtra("callId");
        callerId = getIntent().getStringExtra("callerId");
        callerName = getIntent().getStringExtra("callerName");
        type = getIntent().getStringExtra("type");
        myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        callRef = FirebaseDatabase.getInstance().getReference("calls").child(myUserId);
        acceptButton = findViewById(R.id.acceptButton);
        declineButton = findViewById(R.id.declineButton);
        acceptButton.setOnClickListener(v -> acceptCall());
        declineButton.setOnClickListener(v -> declineCall());

        TextView tvCallerName = findViewById(R.id.tvCallerName);
        // Lấy tên thật của người gọi từ Firebase
        if (callerId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(callerId);
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null) tvCallerName.setText(name);
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        } else {
            tvCallerName.setText(callerName);
        }

        // Phát nhạc chuông khi có cuộc gọi đến
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            );
            mediaPlayer.setDataSource(this, ringtoneUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing ringtone", e);
        }

        // Lấy myUserId thực tế từ Firebase trước khi lắng nghe node và xử lý các thao tác
        new FirebaseMessengerRepository().getCurrentUserId(myUserIdReal -> {
            if (myUserIdReal == null || myUserIdReal.isEmpty()) {
                Toast.makeText(this, "Không lấy được userId, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
                stopRingtone();
                finish();
                return;
            }
            myUserId = myUserIdReal;
            // Lắng nghe node /calls/{myUserId}
            callRef = FirebaseDatabase.getInstance().getReference().child("calls").child(myUserId);
            callRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        if (!isFinishing()) {
                            runOnUiThread(() -> {
                                Toast.makeText(IncomingCallActivity.this, "Cuộc gọi đã kết thúc!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        }
                        return;
                    }
                    if (snapshot.child("status").exists() && "ended".equals(snapshot.child("status").getValue(String.class))) {
                        if (!isFinishing()) {
                            runOnUiThread(() -> {
                                Toast.makeText(IncomingCallActivity.this, "Cuộc gọi đã kết thúc!", Toast.LENGTH_SHORT).show();
                                String toUserId = callerId;
                                if (toUserId == null) {
                                    toUserId = getIntent().getStringExtra("callerId");
                                }
                                if (toUserId == null) {
                                    toUserId = getIntent().getStringExtra("remoteUserId");
                                }
                                if (toUserId == null) {
                                    toUserId = getIntent().getStringExtra("toUserId");
                                }
                                if (toUserId != null) {
                                    Intent intent = new Intent(IncomingCallActivity.this, MessageActivity.class);
                                    intent.putExtra("toUserId", toUserId);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                                finish();
                            });
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        });
    }

    private void acceptCall() {
        acceptButton.setEnabled(false);
        callRef.child("status").setValue("accepted").addOnSuccessListener(aVoid -> {
            Intent intent;
            if ("video".equals(type)) {
                intent = new Intent(this, VideoCallActivity.class);
            } else {
                intent = new Intent(this, AudioCallActivity.class);
            }
            intent.putExtra("callId", callId);
            intent.putExtra("currentUserId", myUserId);
            intent.putExtra("remoteUserId", callerId);
            intent.putExtra("isInitiator", false);
            intent.putExtra("type", type);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            acceptButton.setEnabled(true);
            Toast.makeText(this, "Lỗi khi nhận cuộc gọi, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        });
    }

    private void declineCall() {
        callRef.child("status").setValue("declined").addOnCompleteListener(task -> {
            // Xóa node cuộc gọi của cả 2 bên
            if (myUserId != null) {
                FirebaseDatabase.getInstance().getReference().child("calls").child(myUserId).removeValue();
            }
            if (callerId != null) {
                FirebaseDatabase.getInstance().getReference().child("calls").child(callerId).removeValue();
            }
            // Trở về màn hình chat, truyền đúng toUserId
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("toUserId", callerId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void stopRingtone() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }
} 