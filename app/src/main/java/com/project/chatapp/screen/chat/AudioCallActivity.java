package com.project.chatapp.screen.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.chatapp.R;
import com.project.chatapp.utils.WebRTCClient;
import com.project.chatapp.utils.SignalingClient;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AudioCallActivity extends AppCompatActivity {
    private static final String TAG = "AudioCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private WebRTCClient webRTCClient;
    private SignalingClient signalingClient;
    private String callId;
    private String currentUserId;
    private String remoteUserId;
    private boolean isInitiator;
    private boolean isCallEnded = false;

    private ImageButton endCallButton;
    private ImageButton toggleMicButton;
    private TextView callStatusText;
    private boolean isMicMuted = false;

    private DatabaseReference callRef;
    private TextView userNameText;
    private ImageView userAvatar;
    private TextView callTimerText;
    private Handler timerHandler = new Handler();
    private long callStartTime = 0L;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);

        // Get information from intent
        callId = getIntent().getStringExtra("callId");
        currentUserId = getIntent().getStringExtra("currentUserId");
        remoteUserId = getIntent().getStringExtra("remoteUserId");
        isInitiator = getIntent().getBooleanExtra("isInitiator", false);

        // Initialize UI
        initializeUI();

        // Lắng nghe node cuộc gọi để tự động đóng khi bị xóa
        if (isInitiator) {
            // Caller lắng nghe node của remoteUserId (receiver)
            callRef = FirebaseDatabase.getInstance().getReference().child("calls").child(remoteUserId);
        } else {
            // Receiver lắng nghe node của chính mình
            callRef = FirebaseDatabase.getInstance().getReference().child("calls").child(currentUserId);
        }
        callRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            Toast.makeText(AudioCallActivity.this, "Cuộc gọi đã kết thúc!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AudioCallActivity.this, MessageActivity.class);
                            intent.putExtra("toUserId", remoteUserId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                } else if (snapshot.child("status").exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("accepted".equals(status)) {
                        // Bắt đầu đếm thời gian khi call được accept
                        if (callStartTime == 0L) {
                            callStartTime = SystemClock.elapsedRealtime();
                            timerRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    long elapsed = SystemClock.elapsedRealtime() - callStartTime;
                                    int seconds = (int) (elapsed / 1000);
                                    int minutes = seconds / 60;
                                    seconds = seconds % 60;
                                    if (callTimerText != null)
                                        callTimerText.setText(String.format("%02d:%02d", minutes, seconds));
                                    timerHandler.postDelayed(this, 1000);
                                }
                            };
                            timerHandler.post(timerRunnable);
                        }
                        updateCallStatus("Đang gọi...");
                    } else if ("declined".equals(status) || "ended".equals(status)) {
                        if (!isFinishing()) {
                            runOnUiThread(() -> {
                                Toast.makeText(AudioCallActivity.this, "Cuộc gọi đã kết thúc!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AudioCallActivity.this, MessageActivity.class);
                                intent.putExtra("toUserId", remoteUserId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Check and request permissions
        if (checkPermissions()) {
            initializeWebRTC();
        }

        // Lấy tên đối phương từ Firebase
        String showUserId = isInitiator ? remoteUserId : currentUserId;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(showUserId);
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (name != null && userNameText != null) userNameText.setText(name);
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void initializeUI() {
        endCallButton = findViewById(R.id.endCallButton);
        toggleMicButton = findViewById(R.id.toggleMicButton);
        callStatusText = findViewById(R.id.callStatusText);
        userNameText = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);
        callTimerText = findViewById(R.id.callTimerText);
        if (callTimerText != null) callTimerText.setText("00:00");

        endCallButton.setOnClickListener(v -> endCall());
        toggleMicButton.setOnClickListener(v -> toggleMic());
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void initializeWebRTC() {
        // Initialize WebRTCClient with callback implementation
        webRTCClient = new WebRTCClient(this, new WebRTCClient.WebRTCClientCallback() {
            @Override
            public void onIceCandidateReceived(IceCandidate iceCandidate) {
                if (!isCallEnded && signalingClient != null) {
                    signalingClient.sendIceCandidate(iceCandidate);
                }
            }

            @Override
            public void onConnectionStateChanged(PeerConnection.PeerConnectionState state) {
                runOnUiThread(() -> handleConnectionStateChange(state));
            }

            @Override
            public void onLocalVideoTrackCreated(VideoTrack videoTrack) {
                // Audio call doesn't need video track
                Log.d(TAG, "Ignoring local video track in audio call");
            }

            @Override
            public void onRemoteVideoTrackReceived(VideoTrack videoTrack) {
                // Audio call doesn't need video track
                Log.d(TAG, "Ignoring remote video track in audio call");
            }
        }, "audio");

        // Initialize SignalingClient
        signalingClient = new SignalingClient(callId, currentUserId, remoteUserId,
                new SignalingClient.SignalingCallback() {
                    @Override
                    public void onOfferReceived(SessionDescription offer) {
                        if (!isCallEnded && webRTCClient != null) {
                            webRTCClient.setRemoteDescription(offer);
                            // Create answer when offer is received
                            createAnswer();
                        }
                    }

                    @Override
                    public void onAnswerReceived(SessionDescription answer) {
                        if (!isCallEnded && webRTCClient != null) {
                            webRTCClient.setRemoteDescription(answer);
                        }
                    }

                    @Override
                    public void onIceCandidateReceived(IceCandidate iceCandidate) {
                        if (!isCallEnded && webRTCClient != null) {
                            webRTCClient.addIceCandidate(iceCandidate);
                        }
                    }
                });

        // Initialize connection
        webRTCClient.initializePeerConnection();

        // Create offer if initiator
        if (isInitiator) {
            createOffer();
        }
    }

    private void createOffer() {
        if (!isCallEnded && webRTCClient != null) {
            webRTCClient.createOffer();
            updateCallStatus("Đang kết nối...");
        }
    }

    private void createAnswer() {
        if (!isCallEnded && webRTCClient != null) {
            webRTCClient.createAnswer();
            updateCallStatus("Đang kết nối với người gọi...");
        }
    }

    private void handleConnectionStateChange(PeerConnection.PeerConnectionState state) {
        switch (state) {
            case CONNECTED:
                updateCallStatus("Đã kết nối");
                break;
            case DISCONNECTED:
                updateCallStatus("Mất kết nối");
                break;
            case FAILED:
                updateCallStatus("Kết nối thất bại");
                Toast.makeText(this, "Kết nối thất bại", Toast.LENGTH_SHORT).show();
                endCall();
                break;
        }
    }

    private void updateCallStatus(String status) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isCallEnded && callStatusText != null) {
                callStatusText.setText(status);
            }
        });
    }

    private void toggleMic() {
        if (!isCallEnded && webRTCClient != null) {
            isMicMuted = !isMicMuted;
            webRTCClient.enableAudio(!isMicMuted);
            toggleMicButton.setImageResource(isMicMuted ? 
                R.drawable.ic_mic_off : R.drawable.ic_mic_on);
        }
    }

    private void endCall() {
        if (isCallEnded) return;
        isCallEnded = true;
        // Xóa node của caller
        if (currentUserId != null) {
            FirebaseDatabase.getInstance().getReference().child("calls").child(currentUserId).removeValue();
        }
        // Xóa node của receiver nếu status là 'ringing' (chưa accept/decline)
        DatabaseReference remoteCallRef = FirebaseDatabase.getInstance().getReference().child("calls").child(remoteUserId);
        remoteCallRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("ringing".equals(status) || status == null) {
                    remoteCallRef.removeValue();
                } else if ("accepted".equals(status) || "declined".equals(status)) {
                    remoteCallRef.removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
        String toUserId = remoteUserId;
        if (toUserId == null) {
            toUserId = getIntent().getStringExtra("remoteUserId");
        }
        if (toUserId == null) {
            toUserId = getIntent().getStringExtra("callerId");
        }
        if (toUserId == null) {
            toUserId = getIntent().getStringExtra("currentUserId");
        }
        Log.d("CALL_DEBUG", "AudioCallActivity endCall fallback, toUserId=" + toUserId);
        // Trở về màn hình chat, truyền toUserId
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("toUserId", toUserId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeWebRTC();
            } else {
                Toast.makeText(this, "Cần quyền truy cập microphone để thực hiện cuộc gọi", 
                    Toast.LENGTH_SHORT).show();
                endCall();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
        // KHÔNG gọi endCall() ở đây nữa để tránh tự động kết thúc khi chỉ destroy activity
        if (webRTCClient != null) {
            webRTCClient.release();
        }
        if (signalingClient != null) {
            // cleanup signaling nếu cần
        }
    }

    @Override
    public void onBackPressed() {
        endCall();
        super.onBackPressed();
    }
} 