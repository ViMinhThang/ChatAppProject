package com.project.chatapp.screen.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.project.chatapp.R;
import com.project.chatapp.utils.WebRTCClient;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoTrack;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import android.util.Log;

public class VideoCallActivity extends AppCompatActivity implements WebRTCClient.WebRTCClientCallback {
    private static final String TAG = "VideoCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    private WebRTCClient webRTCClient;
    private String channelName = "testChannel";
    private SurfaceViewRenderer localVideoView;
    private SurfaceViewRenderer remoteVideoView;
    private ImageButton btnMute;
    private ImageButton btnSwitchCamera;
    private ImageButton btnEndCall;
    private boolean isMuted = false;
    private boolean isVideoEnabled = true;
    private String callId;
    private String currentUserId;
    private String remoteUserId;
    private boolean isCallEnded = false;
    private String callType = "video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // Lấy các thông tin cần thiết từ intent
        callId = getIntent().getStringExtra("callId");
        currentUserId = getIntent().getStringExtra("currentUserId");
        remoteUserId = getIntent().getStringExtra("remoteUserId");
        if (getIntent().hasExtra("type")) {
            callType = getIntent().getStringExtra("type");
        }

        // Get channel name from intent if available
        if (getIntent().hasExtra("channelName")) {
            channelName = getIntent().getStringExtra("channelName");
        }

        // Initialize views first
        initializeViews();

        // Check permissions before initializing WebRTC
        if (checkAndRequestPermissions()) {
            initializeWebRTC();
        }
    }

    private boolean checkAndRequestPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                initializeWebRTC();
            } else {
                Toast.makeText(this, "Cần cấp đầy đủ quyền để thực hiện cuộc gọi video", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeViews() {
        localVideoView = findViewById(R.id.local_video_view_container);
        remoteVideoView = findViewById(R.id.remote_video_view_container);
        btnMute = findViewById(R.id.btn_mute);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnEndCall = findViewById(R.id.btnEndCall);

        // Set click listeners
        btnEndCall.setOnClickListener(v -> endCall());
        
        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (webRTCClient != null) {
                webRTCClient.enableAudio(!isMuted);
                btnMute.setImageResource(isMuted ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
            }
        });

        btnSwitchCamera.setOnClickListener(v -> {
            if (webRTCClient != null) {
                webRTCClient.switchCamera();
            }
        });
    }

    private void initializeWebRTC() {
        try {
            webRTCClient = new WebRTCClient(this, this, callType);

            // Initialize video renderers
            try {
                localVideoView.init(webRTCClient.getEglBaseContext(), null);
                localVideoView.setZOrderMediaOverlay(true);
                remoteVideoView.init(webRTCClient.getEglBaseContext(), null);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing video views", e);
            }

            webRTCClient.initializePeerConnection();
            webRTCClient.createOffer();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up WebRTC", e);
            Toast.makeText(this, "Lỗi khởi tạo cuộc gọi video", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void endCall() {
        if (isCallEnded) return;
        isCallEnded = true;
        if (webRTCClient != null) {
            webRTCClient.release();
        }
        // Xóa node call của cả 2 bên trên Firebase
        if (currentUserId != null) {
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child("calls").child(currentUserId).removeValue();
        }
        if (remoteUserId != null) {
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child("calls").child(remoteUserId).removeValue();
        }
        // Trở về màn hình chat, truyền toUserId
        android.content.Intent intent = new android.content.Intent(this, MessageActivity.class);
        intent.putExtra("toUserId", remoteUserId);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onIceCandidateReceived(IceCandidate iceCandidate) {
        // TODO: Send this ice candidate to remote peer through your signaling server
        Log.d(TAG, "ICE candidate received: " + iceCandidate.toString());
    }

    @Override
    public void onConnectionStateChanged(PeerConnection.PeerConnectionState state) {
        Log.d(TAG, "Connection state changed: " + state.toString());
        runOnUiThread(() -> {
            switch (state) {
                case CONNECTED:
                    // Handle connected state
                    break;
                case DISCONNECTED:
                case FAILED:
                    // Handle disconnection
                    endCall();
                    break;
            }
        });
    }

    @Override
    public void onLocalVideoTrackCreated(VideoTrack videoTrack) {
        runOnUiThread(() -> {
            Log.d(TAG, "Local video track created");
            videoTrack.addSink(localVideoView);
        });
    }

    @Override
    public void onRemoteVideoTrackReceived(VideoTrack videoTrack) {
        runOnUiThread(() -> {
            Log.d(TAG, "Remote video track received: " + videoTrack);
            videoTrack.addSink(remoteVideoView);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webRTCClient != null) {
            webRTCClient.release();
        }
        if (localVideoView != null) {
            localVideoView.release();
        }
        if (remoteVideoView != null) {
            remoteVideoView.release();
        }
    }
} 