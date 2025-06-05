package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.adapter.ChatApdater;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatApdater chatApdater;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageView btnSend, btnBack;
    private FirebaseMessengerRepository repo;
    private String toUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activiy_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toUserId = getIntent().getStringExtra("userId");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.back_chat);
        messageList = new ArrayList<>();
        chatApdater = new ChatApdater(messageList);
        recyclerView.setAdapter(chatApdater);
        btnSend.setOnClickListener(v -> sendMessage());

// Thêm tên placeHolder
        TextView chatterName = findViewById(R.id.chatter);
        String userName = getIntent().getStringExtra("userName");
        Log.d("DEBUG", "Received userName: " + userName);
        if (userName != null) {
            chatterName.setText(userName);
        } else {
            Log.d("DEBUG", "userName is null");
        }




        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatsActivity.class));
        });
        repo = new FirebaseMessengerRepository();

        repo.getCurrentUserId(userId -> {
            Log.d("UserID", "My ID: " + userId);

            repo.listenForMessages(userId, toUserId, (from, to, message, timestamp) -> {
                boolean isSentByMe = from.equals(userId);
                ChatMessage chatMessage = new ChatMessage(from, to, message, timestamp);
                chatMessage.setSender(isSentByMe);
                messageList.add(chatMessage);
                chatApdater.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            });
        });

    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, text);
            etMessage.setText("");
        });
    }


}