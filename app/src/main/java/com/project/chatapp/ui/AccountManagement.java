package com.project.chatapp.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.adapter.ChatApdater;
import com.project.chatapp.adapter.SettingAdapter;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.model.SettingNav;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AccountManagement extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SettingAdapter adapter;
    private ChatApdater chatApdater;
    private List<SettingNav> settingNavList;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageView btnSend;

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
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        messageList = new ArrayList<>();
        messageList.add(new ChatMessage("Look at how chocho sleep in my arms!", "16:46", false));
        messageList.add(new ChatMessage("Can I come over?", "16:46", true));
        messageList.add(new ChatMessage("K, I'm on my way", "16:50", true));
        messageList.add(new ChatMessage("Good morning, did you sleep well?", "09:45", false));
        chatApdater = new ChatApdater(messageList);
        recyclerView.setAdapter(chatApdater);
        btnSend.setOnClickListener(v -> sendMessage());

//        settingNavList = new ArrayList<>();
//        settingNavList.add(new SettingNav(R.drawable.account, "account"));
//        settingNavList.add(new SettingNav(R.drawable.chat_nav, "Chats"));
//        settingNavList.add(new SettingNav(R.drawable.appearance, "Appearance"));
//        settingNavList.add(new SettingNav(R.drawable.notification, "Notification"));
//        settingNavList.add(new SettingNav(R.drawable.privacy, "Privacy"));
//        settingNavList.add(new SettingNav(R.drawable.datausage, "Data Usage"));
//        settingNavList.add(new SettingNav(R.drawable.help, "Help"));
//        settingNavList.add(new SettingNav(R.drawable.invite, "Invite Your Friends"));


    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            messageList.add(new ChatMessage(text, "Now", true));
            chatApdater.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            etMessage.setText("");
        }
    }
}