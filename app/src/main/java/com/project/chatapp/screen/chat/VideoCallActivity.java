package com.project.chatapp.screen.chat;

import android.Manifest;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.project.chatapp.R;
import io.agora.rtc2.*;
import io.agora.rtc2.video.VideoCanvas;

public class VideoCallActivity extends AppCompatActivity {
    private RtcEngine agoraEngine;
    private String appId = "9bdd20eacdde48339b1f9eef49ae75f2";
    private String channelName = "testChannel"; // Sẽ truyền động khi gọi
    private int uid = 0;
    private String token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        }, 22);
        if (getIntent().hasExtra("channelName")) {
            channelName = getIntent().getStringExtra("channelName");
        }
        android.widget.Button btnEndCall = findViewById(R.id.btnEndCall);
        btnEndCall.setOnClickListener(v -> finish());
        setupAgoraEngine();
        joinChannel();
    }

    private void setupAgoraEngine() {
        try {
            agoraEngine = RtcEngine.create(getBaseContext(), appId, new IRtcEngineEventHandler() {
                @Override
                public void onUserJoined(int uid, int elapsed) {
                    runOnUiThread(() -> setupRemoteVideo(uid));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void joinChannel() {
        agoraEngine.enableVideo();
        setupLocalVideo();
        agoraEngine.joinChannel(token, channelName, "", uid);
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        agoraEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        container.addView(surfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }
    }
} 