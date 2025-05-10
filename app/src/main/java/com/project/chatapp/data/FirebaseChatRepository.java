package com.project.chatapp.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.project.chatapp.utils.TimeUtils;

public class FirebaseChatRepository {

    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public interface UserIdCallback {
        void onUserIdReceived(String userId);
    }

    public interface MessagesCallback {
        void onMessage(String from, String to, String message, String timestamp);
    }

    public FirebaseChatRepository() {
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

    public void getMessagesForUser(String userId, MessagesCallback callback) {
        mDatabase.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot thread : snapshot.getChildren()) {
                    String key = thread.getKey();
                    if (key != null && key.contains(userId)) {
                        for (DataSnapshot msg : thread.getChildren()) {
                            String from = msg.child("from").getValue(String.class);
                            String to = msg.child("to").getValue(String.class);
                            String message = msg.child("message").getValue(String.class);
                            String timestamp = msg.child("timestamp").getValue(String.class);

                            callback.onMessage(from, to, message, TimeUtils.getTimeAgo(timestamp));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MessageError", error.getMessage());
            }
        });
    }
}
