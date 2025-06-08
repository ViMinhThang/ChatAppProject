package com.project.chatapp.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;

import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseMessengerRepository {

    private final DatabaseReference mDatabase;
    private final FirebaseAuth mAuth;

    public FirebaseMessengerRepository() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public interface SmartReplyCallback {
        void onSuggestions(List<String> suggestions);
    }

    public interface UserIdCallback {
        void onUserIdReceived(@Nullable String userId);
    }

    public interface UserNameCallback {
        void onUserNameReceived(@Nullable String userName);
    }

    public interface MessagesCallback {
        void onMessage(String from, String to, String message, String timestamp, String messageId);
    }

    public interface FriendListCallback {
        void onFriendListReceived(List<ContactModel> friends);
    }
    public void getCurrentUserId(UserIdCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("FirebaseAuth", "Current user is null.");
            callback.onUserIdReceived(null);
            return;
        }

        String userPhone = currentUser.getPhoneNumber();
        Log.d("FirebaseAuth", "User phone: " + userPhone);

        Query query = mDatabase.child("users").orderByChild("phone").equalTo(userPhone);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    callback.onUserIdReceived(userId);

                    // Lưu userId vào SharedPreferences
                    Context context = getApplicationContext();
                    if (context != null) {
                        SharedPreferences prefs = context.getSharedPreferences("ChatAppPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("currentUserId", userId);
                        editor.apply();
                    }
                    return;
                }
                callback.onUserIdReceived(null); // Không tìm thấy
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "getCurrentUserId: " + error.getMessage());
                callback.onUserIdReceived(null);
            }
        });
    }

    // Thêm phương thức để lấy context
    private Context getApplicationContext() {
        try {
            return com.google.firebase.FirebaseApp.getInstance().getApplicationContext();
        } catch (Exception e) {
            Log.e("FirebaseRepository", "Failed to get application context", e);
            return null;
        }
    }

    public void getUserNameById(String userId, UserNameCallback callback) {
        mDatabase.child("users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onUserNameReceived(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "getUserNameById: " + error.getMessage());
                        callback.onUserNameReceived(null);
                    }
                });
    }

    public void sendMessage(String from, String to, String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String chatId = from.compareTo(to) < 0 ? from + "_" + to : to + "_" + from;

        DatabaseReference chatRef = mDatabase.child("messages").child(chatId);
        DatabaseReference newMsgRef = chatRef.push();
        String messageId = newMsgRef.getKey();

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("fromId", from);
        msgData.put("toId", to);
        msgData.put("content", message);
        msgData.put("timeStamp", timestamp);

        newMsgRef.setValue(msgData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("SendMessage", "Message sent");

                Map<String, Object> lastMessage = new HashMap<>();
                lastMessage.put("last_content", message); // Chỉ lưu nội dung tin nhắn
                lastMessage.put("last_content_time", timestamp);

                // Cập nhật cho cả người gửi và người nhận
                mDatabase.child("users").child(from).child("chats").child(to).updateChildren(lastMessage);
                mDatabase.child("users").child(to).child("chats").child(from).updateChildren(lastMessage);

                // Tăng unread_count cho người nhận
                mDatabase.child("users").child(to).child("chats").child(from)
                        .child("unread_count").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Long unread = mutableData.getValue(Long.class);
                                if (unread == null) {
                                    unread = 0L;
                                }
                                mutableData.setValue(unread + 1);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean committed,
                                                   @Nullable DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    Log.e("SendMessage", "Transaction failed", databaseError.toException());
                                }
                            }
                        });
            } else {
                Log.e("SendMessage", "Failed to send message", task.getException());
            }
        });

    }

    public void generateSmartReplies(String chatId, String userLocalId, SmartReplyCallback callback) {
        DatabaseReference chatRef = mDatabase.child("messages").child(chatId);

        chatRef.orderByKey().limitToLast(10).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<TextMessage> conversation = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    ChatMessage chatMsg = snapshot.getValue(ChatMessage.class);

                    if (chatMsg != null && !chatMsg.getContent().contains("http")) {
                        String fromId = chatMsg.getFromId();
                        String content = chatMsg.getContent();
                        long timestamp = Long.parseLong(chatMsg.getTimeStamp());

                        if (fromId.equals(userLocalId)) {
                            conversation.add(TextMessage.createForLocalUser(content, timestamp));
                        } else {
                            conversation.add(TextMessage.createForRemoteUser(content, timestamp, fromId));
                        }
                    }
                }

                SmartReply.getClient().suggestReplies(conversation)
                        .addOnSuccessListener(result -> {
                            List<String> suggestions = new ArrayList<>();
                            for (SmartReplySuggestion suggestion : result.getSuggestions()) {
                                suggestions.add(suggestion.getText());
                            }
                            callback.onSuggestions(suggestions);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("SmartReply", "Failed to get suggestions", e);
                            callback.onSuggestions(new ArrayList<>());
                        });
            } else {
                callback.onSuggestions(new ArrayList<>());
            }
        });
    }


    public void listenForMessages(String fromId, String toId, MessagesCallback callback) {
        String chatId = fromId.compareTo(toId) < 0 ? fromId + "_" + toId : toId + "_" + fromId;
        DatabaseReference chatRef = mDatabase.child("messages").child(chatId);

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String from = snapshot.child("fromId").getValue(String.class);
                String to = snapshot.child("toId").getValue(String.class);
                String message = snapshot.child("content").getValue(String.class);
                String timestamp = snapshot.child("timeStamp").getValue(String.class);
                String messageId = snapshot.getKey();

                if (from != null && to != null && message != null && timestamp != null && messageId != null) {
                    String readableTime = TimeUtils.getTimeAgo(timestamp);
                    callback.onMessage(from, to, message, readableTime, messageId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "listenForMessages: " + error.getMessage());
            }
        });
    }

    // DS bạn bè hiển thị lên contact
    public void getFriendList(FriendListCallback callback) {
        getCurrentUserId(userId -> {
            if (userId == null) {
                callback.onFriendListReceived(new ArrayList<>());
                return;
            }

            mDatabase.child("users").child(userId).child("chats")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<ContactModel> friends = new ArrayList<>();
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                String friendId = chatSnapshot.getKey();
                                if (friendId != null) {
                                    mDatabase.child("users").child(friendId)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                                                    String name = friendSnapshot.child("name").getValue(String.class);
                                                    String status = friendSnapshot.child("status").getValue(String.class);
                                                    String profilePicture = friendSnapshot.child("profile_picture").getValue(String.class);

                                                    ContactModel friend = new ContactModel(name, status, profilePicture);
                                                    friends.add(friend);

                                                    // Chỉ callback khi đã load đủ số bạn
                                                    if (friends.size() == snapshot.getChildrenCount()) {
                                                        callback.onFriendListReceived(friends);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("Firebase", "getFriendList: " + error.getMessage());
                                                }
                                            });
                                }
                            }

                            // Trường hợp không có bạn nào
                            if (snapshot.getChildrenCount() == 0) {
                                callback.onFriendListReceived(friends);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("Firebase", "getFriendList (outer): " + error.getMessage());
                            callback.onFriendListReceived(new ArrayList<>());
                        }
                    });
        });
    }
}
