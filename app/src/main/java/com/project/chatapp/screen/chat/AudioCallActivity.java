package com.project.chatapp.screen.chat;

import android.Manifest;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.project.chatapp.R;

import io.agora.rtc2.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class AudioCallActivity extends AppCompatActivity {
    private RtcEngine agoraEngine;
    private String appId = "c063b998752f4600801538aa38f12e56";
    private String channelName = "testChannel"; // Sẽ truyền động khi gọi
    private int uid = 0;
    private String token = null;
    private String myUserId;
    private String otherUserId;
    private Handler timerHandler = new Handler();
    private long callStartTime = 0;
    private boolean isCounting = false;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCounting) {
                long elapsed = SystemClock.elapsedRealtime() - callStartTime;
                int seconds = (int) (elapsed / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                android.widget.TextView tvTimer = findViewById(R.id.tvTimer);
                if (tvTimer != null) {
                    tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
                }
                timerHandler.postDelayed(this, 1000);
            }
        }
    };
    private DatabaseReference joinedRef;
    private DatabaseReference otherJoinedRef;
    private boolean otherJoined = false;
    private boolean iJoined = false;
    private ValueEventListener otherJoinedListener;
    private DatabaseReference endRef;
    private ValueEventListener endListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call); // Sử dụng layout chờ
        android.widget.TextView tvStatus = findViewById(R.id.tvStatus);
        tvStatus.setText("Đang gọi...");
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO
        }, 22);
        if (getIntent().hasExtra("channelName")) {
            channelName = getIntent().getStringExtra("channelName");
        }
        // Hiển thị tên người nhận nếu có
        String name = getIntent().getStringExtra("name");
        if (name != null) {
            android.widget.TextView tvName = findViewById(R.id.tvName);
            tvName.setText(name);
        }
        myUserId = getIntent().getStringExtra("fromUserId");
        otherUserId = getIntent().getStringExtra("toUserId");
        Log.d("CALL_DEBUG", "AudioCallActivity onCreate: channelName=" + channelName + ", myUserId=" + myUserId + ", otherUserId=" + otherUserId);
        // Gán uid duy nhất cho mỗi user
        uid = getUidFromUserId(myUserId);
        Log.d("AGORA_DEBUG", "onCreate: myUserId=" + myUserId + ", otherUserId=" + otherUserId + ", channelName=" + channelName + ", uid=" + uid);
        android.widget.Button btnEndCall = findViewById(R.id.btnEndCall);
        btnEndCall.setOnClickListener(v -> endCall());
        setupAgoraEngine();
        joinChannel();
        // Chuẩn bị ref cho joined status
        joinedRef = FirebaseDatabase.getInstance().getReference().child("calls_status").child(myUserId).child("joined");
        otherJoinedRef = FirebaseDatabase.getInstance().getReference().child("calls_status").child(otherUserId).child("joined");
        // Lắng nghe trạng thái joined của đối phương
        otherJoinedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean joined = snapshot.getValue(Boolean.class);
                if (joined != null && joined) {
                    otherJoined = true;
                    if (iJoined) {
                        Log.d("AGORA_DEBUG", "Cả hai đã joined, chuyển sang layout đếm thời gian (Firebase)");
                        switchToIncallLayout(name); // name đã lấy từ intent
                        startCallTimer();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        otherJoinedRef.addValueEventListener(otherJoinedListener);
        // Lắng nghe end call
        endRef = FirebaseDatabase.getInstance().getReference().child("calls_status").child(myUserId).child("end");
        endListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean ended = snapshot.getValue(Boolean.class);
                if (ended != null && ended) {
                    Log.d("CALL_DEBUG", "Nhận tín hiệu end call, đóng activity");
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        };
        endRef.addValueEventListener(endListener);
    }

    private void setupAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(getBaseContext(), appId, new IRtcEngineEventHandler() {
                @Override
                public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                    Log.d("AGORA_DEBUG", "onJoinChannelSuccess: channel=" + channel + ", uid=" + uid);
                    iJoined = true;
                    joinedRef.setValue(true);
                    // Kiểm tra nếu đối phương đã join thì chuyển layout
                    if (otherJoined) {
                        runOnUiThread(() -> {
                            String name = getIntent().getStringExtra("name");
                            Log.d("AGORA_DEBUG", "Cả hai đã joined, chuyển sang layout đếm thời gian (onJoinChannelSuccess)");
                            switchToIncallLayout(name);
                            startCallTimer();
                        });
                    }
                }
                @Override
                public void onUserJoined(int uid, int elapsed) {
                    Log.d("AGORA_DEBUG", "onUserJoined: uid=" + uid);
                    otherJoined = true;
                    // Kiểm tra nếu mình đã join thì chuyển layout
                    if (iJoined) {
                        runOnUiThread(() -> {
                            String name = getIntent().getStringExtra("name");
                            Log.d("AGORA_DEBUG", "Cả hai đã joined, chuyển sang layout đếm thời gian (onUserJoined)");
                            switchToIncallLayout(name);
                            startCallTimer();
                        });
                    }
                }
                @Override
                public void onUserOffline(int uid, int reason) {
                    Log.d("AGORA_DEBUG", "onUserOffline: uid=" + uid);
                    runOnUiThread(() -> {
                        // Có thể chuyển lại layout cũ hoặc kết thúc call
                        isCounting = false;
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinChannel() {
        Log.d("AGORA_DEBUG", "joinChannel: channelName=" + channelName + ", uid=" + uid);
        agoraEngine.enableAudio();
        agoraEngine.disableVideo();
        agoraEngine.joinChannel(token, channelName, "", uid);
    }

    private void endCall() {
        if (otherUserId != null && !otherUserId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference().child("calls_status").child(otherUserId).child("end").setValue(true);
            FirebaseDatabase.getInstance().getReference().child("calls").child(otherUserId).removeValue();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCounting = false;
        timerHandler.removeCallbacks(timerRunnable);
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
        if (joinedRef != null) joinedRef.removeValue();
        if (otherJoinedRef != null && otherJoinedListener != null) {
            otherJoinedRef.removeEventListener(otherJoinedListener);
        }
        if (endRef != null && endListener != null) {
            endRef.removeEventListener(endListener);
            endRef.setValue(false);
        }
    }

    private int getUidFromUserId(String userId) {
        if (userId == null) return 0;
        return Math.abs(userId.hashCode());
    }

    private void switchToIncallLayout(String name) {
        Log.d("CALL_DEBUG", "switchToIncallLayout được gọi: name=" + name);
        setContentView(R.layout.activity_audio_call_incall);
        Log.d("CALL_DEBUG", "setContentView activity_audio_call_incall thành công");
        android.widget.TextView tvName = findViewById(R.id.tvName);
        Log.d("CALL_DEBUG", "findViewById tvName: " + (tvName != null));
        if (name != null && tvName != null) tvName.setText(name);
        android.widget.Button btnEndCall = findViewById(R.id.btnEndCall);
        Log.d("CALL_DEBUG", "findViewById btnEndCall: " + (btnEndCall != null));
        if (btnEndCall != null) btnEndCall.setOnClickListener(v -> endCall());
        startCallTimer();
    }

    private void startCallTimer() {
        if (!isCounting) {
            isCounting = true;
            callStartTime = SystemClock.elapsedRealtime();
            timerHandler.post(timerRunnable);
        }
    }
} 