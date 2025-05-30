package com.project.chatapp;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project.chatapp.screen.chat.IncomingCallActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM_DEBUG", "onMessageReceived: " + remoteMessage.getData());
        if (remoteMessage.getData() != null && "call".equals(remoteMessage.getData().get("type"))) {
            Intent intent = new Intent(this, IncomingCallActivity.class);
            intent.putExtra("callerName", remoteMessage.getData().get("callerName"));
            intent.putExtra("channelName", remoteMessage.getData().get("channelName"));
            intent.putExtra("fromUserId", remoteMessage.getData().get("fromUserId"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
} 