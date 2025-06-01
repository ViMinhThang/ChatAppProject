package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.project.chatapp.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatApdater chatApdater;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageView btnSend, btnBack, btnSearch, btnClearSearch, btnPrevious, btnNext;
    private FirebaseMessengerRepository repo;
    private String toUserId;

    // Search components
    private LinearLayout searchBarView, searchNavigationView;
    private EditText etSearchInput;
    private TextView tvSearchResult;

    // Search variables
    private boolean isSearchMode = false;
    private String currentSearchQuery = "";
    private int currentSearchIndex = 0;
    private List<Integer> searchPositions;
    private List<ChatMessage> searchResults;

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

        initializeViews();
        setupRecyclerView();
        setupEventListeners();
        setupFirebase();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.back_chat);
        btnSearch = findViewById(R.id.btnSearch);

        searchBarView = findViewById(R.id.searchBarView);
        searchNavigationView = findViewById(R.id.searchNavigationView);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        tvSearchResult = findViewById(R.id.tvSearchResult);

        messageList = new ArrayList<>();
        searchResults = new ArrayList<>();
        searchPositions = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatApdater = new ChatApdater(messageList, this::onMessageClick);
        recyclerView.setAdapter(chatApdater);
    }

    private void setupEventListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        btnBack.setOnClickListener(v -> {
            if (isSearchMode) {
                exitSearchMode();
            } else {
                startActivity(new Intent(this, ChatsActivity.class));
            }
        });

        btnSearch.setOnClickListener(v -> toggleSearchMode());
        btnClearSearch.setOnClickListener(v -> clearSearch());
        btnPrevious.setOnClickListener(v -> navigateSearchResult(-1));
        btnNext.setOnClickListener(v -> navigateSearchResult(1));

        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFirebase() {
        repo = new FirebaseMessengerRepository();

        repo.getCurrentUserId(userId -> {
            Log.d("UserID", "My ID: " + userId);

            repo.listenForMessages(userId, toUserId, (from, to, message, timestamp, messageId) -> {
                boolean isSentByMe = from.equals(userId);
                ChatMessage chatMessage = new ChatMessage(from, to, message, timestamp);
                chatMessage.setSender(isSentByMe);
                messageList.add(chatMessage);

                if (isSearchMode && !currentSearchQuery.isEmpty()) {
                    performSearch(currentSearchQuery);
                } else {
                    chatApdater.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
        });
    }


    private void toggleSearchMode() {
        if (isSearchMode) {
            exitSearchMode();
        } else {
            enterSearchMode();
        }
    }

    private void enterSearchMode() {
        isSearchMode = true;
        searchBarView.setVisibility(View.VISIBLE);
        etSearchInput.requestFocus();

        etSearchInput.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 100);
    }

    private void exitSearchMode() {
        isSearchMode = false;
        searchBarView.setVisibility(View.GONE);
        searchNavigationView.setVisibility(View.GONE);
        etSearchInput.setText("");
        currentSearchQuery = "";
        currentSearchIndex = 0;
        searchResults.clear();
        searchPositions.clear();

        chatApdater.updateMessages(messageList, "", -1);

        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchInput.getWindowToken(), 0);
    }

    private void clearSearch() {
        etSearchInput.setText("");
    }

    private void performSearch(String query) {
        currentSearchQuery = query.trim();

        if (currentSearchQuery.isEmpty()) {
            searchNavigationView.setVisibility(View.GONE);
            searchResults.clear();
            searchPositions.clear();
            chatApdater.updateMessages(messageList, "", -1);
            return;
        }

        searchResults.clear();
        searchPositions.clear();

        for (int i = 0; i < messageList.size(); i++) {
            ChatMessage message = messageList.get(i);
            if (message.getContent() != null &&
                    message.getContent().toLowerCase().contains(currentSearchQuery.toLowerCase())) {
                searchResults.add(message);
                searchPositions.add(i);
            }
        }

        updateSearchUI();

        chatApdater.updateMessages(messageList, currentSearchQuery,
                searchPositions.isEmpty() ? -1 : searchPositions.get(0));

        if (!searchPositions.isEmpty()) {
            currentSearchIndex = 0;
            scrollToSearchResult(searchPositions.get(0));
        }
    }

    private void updateSearchUI() {
        if (searchResults.isEmpty()) {
            searchNavigationView.setVisibility(View.GONE);
            tvSearchResult.setText("Không tìm thấy");
        } else {
            searchNavigationView.setVisibility(View.VISIBLE);
            tvSearchResult.setText((currentSearchIndex + 1) + "/" + searchResults.size());

            // Enable/disable navigation buttons
            btnPrevious.setEnabled(currentSearchIndex > 0);
            btnNext.setEnabled(currentSearchIndex < searchResults.size() - 1);

            btnPrevious.setAlpha(currentSearchIndex > 0 ? 1.0f : 0.5f);
            btnNext.setAlpha(currentSearchIndex < searchResults.size() - 1 ? 1.0f : 0.5f);
        }
    }

    private void navigateSearchResult(int direction) {
        if (searchPositions.isEmpty()) return;

        int newIndex = currentSearchIndex + direction;
        if (newIndex >= 0 && newIndex < searchPositions.size()) {
            currentSearchIndex = newIndex;
            updateSearchUI();

            int messagePosition = searchPositions.get(currentSearchIndex);
            chatApdater.updateMessages(messageList, currentSearchQuery, messagePosition);
            scrollToSearchResult(messagePosition);
        }
    }

    private void scrollToSearchResult(int position) {
        recyclerView.post(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(position,
                        recyclerView.getHeight() / 2);
            }
        });
    }

    private void onMessageClick(int position) {
        if (isSearchMode && !searchPositions.isEmpty()) {
            for (int i = 0; i < searchPositions.size(); i++) {
                if (searchPositions.get(i) == position) {
                    currentSearchIndex = i;
                    updateSearchUI();
                    chatApdater.updateMessages(messageList, currentSearchQuery, position);
                    break;
                }
            }
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, text);
            etMessage.setText("");
        });
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            exitSearchMode();
        } else {
            super.onBackPressed();
        }
    }
}
