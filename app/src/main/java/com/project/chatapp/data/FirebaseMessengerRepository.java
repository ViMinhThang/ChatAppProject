package com.project.chatapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessengerRepository {

    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public interface UserIdCallback {
        void onUserIdReceived(String userId);
    }

    public interface MessagesCallback {
        void onMessage(String from, String to, String message, String timestamp);
    }

    public FirebaseMessengerRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public void getCurrentUserId(UserIdCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userPhoneNumber = currentUser.getPhoneNumber();

        Query query = mDatabase.child("users").orderByChild("phone").equalTo(userPhoneNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    callback.onUserIdReceived(userId);
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    public void sendMessage(String from, String to, String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String chatId = from.compareTo(to) < 0 ? from + "_" + to : to + "_" + from;

        DatabaseReference chatRef = mDatabase.child("messages").child(chatId);
        DatabaseReference newMsgRef = chatRef.push();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("from", from);
        messageData.put("to", to);
        messageData.put("message", message);
        messageData.put("timestamp", timestamp);

        newMsgRef.setValue(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("SendMessage", "Message sent");
                Map<String, Object> lastMsgUpdate = new HashMap<>();
                lastMsgUpdate.put("last_message", message);
                lastMsgUpdate.put("last_message_time", timestamp);
                mDatabase.child("users").child(from).child("chats").child(to).updateChildren(lastMsgUpdate);
                mDatabase.child("users").child(to).child("chats").child(from).updateChildren(lastMsgUpdate);
            } else {
                Log.e("SendMessage", "Failed", task.getException());
            }
        });
    }

    public void listenForMessages(String fromId, String toId, MessagesCallback callback) {
        String chatId = fromId.compareTo(toId) < 0 ? fromId + "_" + toId : toId + "_" + fromId;
        DatabaseReference chatRef = mDatabase.child("messages").child(chatId);

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String from = snapshot.child("from").getValue(String.class);
                String to = snapshot.child("to").getValue(String.class);
                String message = snapshot.child("message").getValue(String.class);
                String timestamp = snapshot.child("timestamp").getValue(String.class);

                callback.onMessage(from, to, message, TimeUtils.getTimeAgo(timestamp));
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

}
