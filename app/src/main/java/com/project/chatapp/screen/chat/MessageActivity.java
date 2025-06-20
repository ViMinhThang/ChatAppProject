package com.project.chatapp.screen.chat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.adapter.ChatAdapter;
import com.project.chatapp.data.ChatsRepository;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.model.Chat.CustomAdapterRVChats;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.model.CallModel;
import com.project.chatapp.model.Contact.contact.ContactModel;
import com.project.chatapp.screen.location.PickLocationActivity;
import com.project.chatapp.utils.ChatUitls;
import com.project.chatapp.utils.CloudinaryHelper;
import com.project.chatapp.utils.VoiceMessageManager;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_VIDEO_REQUEST = 102;
    private static final int RECORD_PERMISSION_CODE = 1001;
    private ImageView btnVoice;
    private LinearLayout inputContainer, recordingStateView;
    private TextView tvRecordingTimer, btnCancelRecording, btnSendRecording;
    private ImageView ivRecordingIcon;
    private VoiceMessageManager voiceMessageManager;
    private CountDownTimer recordingTimer;
    private final long recordingMinimumDuration = 1000L;
    // UI Components
    private RecyclerView recyclerView;
    private EditText etMessage, etSearchInput;
    private ImageView btnSend, btnBack, btnSendImg, btnCall, btnVideoCall, btnAdd;
    private ImageView btnSearch, btnClearSearch, btnPrevious, btnNext;
    private TextView tvSearchResult;
    private LinearLayout searchBarView, searchNavigationView;
    private ProgressDialog progressDialog;

    // Data and Adapter
    private final List<ChatMessage> messageList = new ArrayList<>();

    private ChatAdapter chatAdapter;
    private FirebaseMessengerRepository repo;
    private String toUserId;
    private DatabaseReference mDatabase;
    // Search
    private ActivityResultLauncher<Intent> settingsLauncher;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private boolean isSearchMode = false;
    private List<Integer> searchResultIndexes = new ArrayList<>();
    private int currentSearchIndex = 0;
    private String currentSearchQuery = "";
    private List<ChatMessage> searchResults;
    private List<Integer> searchPositions;
    private boolean isReturningFromSettings = false;

    // Media
    private static final int STORAGE_PERMISSION_CODE = 103;
    private Uri pendingMediaUri;

    //Contact
    private ContactModel contact;

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
        repo = new FirebaseMessengerRepository();

        // Nhận dữ liệu từ Intent
        contact = getIntent().getParcelableExtra("contact");

        if (contact != null) {
            toUserId = contact.getId(); // ID của người nhận để load tin nhắn
            TextView chatter = findViewById(R.id.chatter);
            chatter.setText(contact.getName());
        }else if (toUserId != null) {
            // Lấy tên từ repo nếu chỉ có ID
            repo.getUserNameById(toUserId, name -> {
                ((TextView) findViewById(R.id.chatter)).setText(name != null ? name : "Người dùng");
            });
        }

        initViews();
        setupRecyclerView();
        setupFirebase();
        setupEventListeners();
        setupPermissionLaunchers();
        loadMessages();

        setupVoiceMessage();
        repo.getCurrentUserId(myUserId -> {
            if (myUserId != null) {
                mDatabase.child("users").child(myUserId).child("chats").child(toUserId)
                        .child("unread_count").setValue(0)
                        .addOnFailureListener(e -> Log.e("MessageActivity", "Failed to reset unread_count", e));
            }
        });
    }



    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        etSearchInput = findViewById(R.id.etSearchInput);
        btnSend = findViewById(R.id.btnSend);
        btnSendImg = findViewById(R.id.btnSendImg);
        btnBack = findViewById(R.id.back_chat);
        btnCall = findViewById(R.id.call);
        btnVideoCall = findViewById(R.id.videoCall);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnAdd = findViewById(R.id.btnAdd);
        btnNext = findViewById(R.id.btnNext);
        tvSearchResult = findViewById(R.id.tvSearchResult);
        searchBarView = findViewById(R.id.searchBarView);
        searchNavigationView = findViewById(R.id.searchNavigationView);
        progressDialog = new ProgressDialog(this);
        searchResults = new ArrayList<>();
        searchPositions = new ArrayList<>();
        btnVoice = findViewById(R.id.btnVoice);
        inputContainer = findViewById(R.id.inputContainer);
        recordingStateView = findViewById(R.id.recordingStateView);
        tvRecordingTimer = findViewById(R.id.tvRecordingTimer);
        btnCancelRecording = findViewById(R.id.btnCancelRecording);
        btnSendRecording = findViewById(R.id.btnSendRecording);
        ivRecordingIcon = findViewById(R.id.ivRecordingIcon);

    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messageList, this::onMessageClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupEventListeners() {
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    btnSend.setVisibility(View.VISIBLE);
                    btnVoice.setVisibility(View.GONE);
                } else {
                    btnSend.setVisibility(View.GONE);
                    btnVoice.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnSend.setOnClickListener(v -> sendMessage());

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatsActivity.class));
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddOptionsMenu(view); // gọi hàm riêng
            }
        });
        btnSendImg.setOnClickListener(v -> checkStoragePermission());
        btnCall.setOnClickListener(v -> {
            repo.getCurrentUserId(myUserId -> {
                if (myUserId == null || myUserId.isEmpty()) {
                    android.widget.Toast.makeText(this, "Không lấy được userId, vui lòng đăng nhập lại!", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                // Reset trạng thái end call về false cho cả hai phía
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                        .child("calls_status").child(toUserId).child("end").setValue(false);
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                        .child("calls_status").child(myUserId).child("end").setValue(false);
                String channelName = myUserId.compareTo(toUserId) < 0 ? myUserId + "_" + toUserId : toUserId + "_" + myUserId;
                ChatsRepository chatsRepository = new ChatsRepository();
                chatsRepository.getUserNameById(myUserId, myName -> {
                    chatsRepository.getUserNameById(toUserId, name -> {
                        long timestamp = System.currentTimeMillis();
                        CallModel call = new CallModel(myUserId, toUserId, "audio", channelName, myName, timestamp);
                        com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                                .child("calls")
                                .child(toUserId)
                                .setValue(call)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("CALL_DEBUG", "Gọi AudioCallActivity: channelName=" + channelName + ", myUserId=" + myUserId + ", toUserId=" + toUserId);
                                        Intent intent = new Intent(this, AudioCallActivity.class);
                                        intent.putExtra("channelName", channelName);
                                        intent.putExtra("name", name);
                                        intent.putExtra("fromUserId", myUserId);
                                        intent.putExtra("toUserId", toUserId);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(this, "Tạo cuộc gọi thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                });
            });
        });
        btnVideoCall.setOnClickListener(v -> {
            buildChannelName(channelName -> {
                if (channelName == null) {
                    Toast.makeText(this, "Không lấy được userId, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
                    return;
                }
                ChatsRepository chatsRepository = new ChatsRepository();
                chatsRepository.getUserNameById(toUserId, name -> {
                    Intent intent = new Intent(this, VideoCallActivity.class);
                    intent.putExtra("channelName", channelName);
                    intent.putExtra("name", name);
                    startActivity(intent);
                });
            });
        });
        if (isSearchMode) {
            exitSearchMode();
        }
        btnSearch.setOnClickListener(v -> toggleSearchMode());
        btnClearSearch.setOnClickListener(v -> clearSearch());
        btnPrevious.setOnClickListener(v -> navigateSearchResult(-1));
        btnNext.setOnClickListener(v -> navigateSearchResult(1));
        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        setupPermissionLaunchers();
        TextView chatter = findViewById(R.id.chatter);
        ChatsRepository chatsRepository = new ChatsRepository();
        chatsRepository.getUserNameById(toUserId, name -> {
            if (name != null) {
                chatter.setText(name);
            } else {
                chatter.setText("Unknown");
            }
        });
    }

    private void setupFirebase() {
        repo = new FirebaseMessengerRepository();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        repo.getCurrentUserId(userId -> {
            Log.d("UserID", "My ID: " + userId);

            if (userId == null || toUserId == null) {
                Log.e("ChatDebug", "Lỗi: userId hoặc toUserId bị null!");
                return;
            }

            repo.listenForMessages(userId, toUserId, (String from, String to, String message, String timestamp, String messageId) -> {
                boolean isSentByMe = from.equals(userId);
                ChatMessage chatMessage = new ChatMessage(from, to, message, timestamp);
                chatMessage.setSender(isSentByMe);
                messageList.add(chatMessage);

                if (isSearchMode && !currentSearchQuery.isEmpty()) {
                    performSearch(currentSearchQuery);
                } else {
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
            String chatId = ChatUitls.getChatId(userId, toUserId);

            repo.generateSmartReplies(chatId, userId, suggestions -> {
                runOnUiThread(() -> {
                    if (!suggestions.isEmpty()) {
                        etMessage.setHint(String.join(" | ", suggestions));
                    } else {
                        etMessage.setHint("Nhập tin nhắn...");
                    }
                });
            });
        });
    }

    private void sendCordinate(String corr) {

        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, corr);
            etMessage.setText("");
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        repo.getCurrentUserId(fromUserId -> {
            String chatId = ChatUitls.getChatId(fromUserId, toUserId);
            repo.sendMessage(fromUserId, toUserId, text);
            etMessage.setText("");
            repo.generateSmartReplies(chatId, fromUserId, suggestions -> {
                runOnUiThread(() -> {
                    if (!suggestions.isEmpty()) {
                        etMessage.setHint(String.join(" | ", suggestions));
                    } else {
                        etMessage.setHint("Nhập tin nhắn...");
                    }
                });
            });
        });
    }

    private void uploadMediaToCloudinary(String filePath) {
        progressDialog.show();
        Log.d("MessageActivity", "Starting upload to Cloudinary: " + filePath);

        CloudinaryHelper.uploadMedia(this, filePath, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String url) {
                Log.d("MessageActivity", "Upload success. URL: " + url);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    sendMediaMessage(url);
                });
            }

            @Override
            public void onError(String error) {
                Log.e("MessageActivity", "Upload error: " + error);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MessageActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void sendMediaMessage(String mediaUrl) {
        Log.d("MessageActivity", "Sending media message: " + mediaUrl);
        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, mediaUrl);
            Log.d("MessageActivity", "Media message sent from " + fromUserId + " to " + toUserId);
        });
    }

    private void performSearch(String query) {
        currentSearchQuery = query.trim();

        if (currentSearchQuery.isEmpty()) {
            searchNavigationView.setVisibility(View.GONE);
            searchResults.clear();
            searchPositions.clear();
            chatAdapter.updateMessages(messageList, "", -1);
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

        chatAdapter.updateMessages(messageList, currentSearchQuery,
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
                    chatAdapter.updateMessages(messageList, currentSearchQuery, position);
                    break;
                }
            }
        }
        ;
    }

    private void navigateSearchResult(int direction) {
        if (searchPositions.isEmpty()) return;

        int newIndex = currentSearchIndex + direction;
        if (newIndex >= 0 && newIndex < searchPositions.size()) {
            currentSearchIndex = newIndex;
            updateSearchUI();

            int messagePosition = searchPositions.get(currentSearchIndex);
            chatAdapter.updateMessages(messageList, currentSearchQuery, messagePosition);
            scrollToSearchResult(messagePosition);
        }
    }

    private void clearSearch() {
        etSearchInput.setText("");
        searchNavigationView.setVisibility(View.GONE);
        searchResultIndexes.clear();
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                        },
                        STORAGE_PERMISSION_CODE);
            } else {
                openImageVideoPicker();
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                openImageVideoPicker();
            }
        }
    }

    private void openImageVideoPicker() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select Image or Video"), PICK_IMAGE_VIDEO_REQUEST);
        } catch (Exception e) {
            Log.e("MessageActivity", "Error opening picker: " + e.getMessage());
            Toast.makeText(this, "Error opening media picker", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (isSearchMode) {
            exitSearchMode();
        }
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

        chatAdapter.updateMessages(messageList, "", -1);

        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchInput.getWindowToken(), 0);
    }

    private void toggleSearchMode() {
        if (isSearchMode) {
            exitSearchMode();
        } else {
            enterSearchMode();
        }
        // Thêm đoạn này để hiển thị tên người đang nhắn
        TextView chatter = findViewById(R.id.chatter);
        ChatsRepository chatsRepository = new ChatsRepository();
        chatsRepository.getUserNameById(toUserId, name -> {
            if (name != null) {
                chatter.setText(name);
            } else {
                chatter.setText("Unknown");
            }
        });
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

    private void setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean isGranted : result.values()) {
                        if (!isGranted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        // startCamera();
                    } else {
                        showPermissionDeniedDialog();
                    }
                });
    }

    private void buildChannelName(ChannelNameCallback callback) {
        // Lấy userId hiện tại từ FirebaseAuth qua repo
        repo.getCurrentUserId(myId -> {
            if (myId == null || myId.isEmpty()) {
                callback.onChannelNameBuilt(null);
                return;
            }
            String channelName = myId.compareTo(toUserId) < 0 ? myId + "_" + toUserId : toUserId + "_" + myId;
            callback.onChannelNameBuilt(channelName);
        });
    }

    private void showAddOptionsMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ActivityCompat.requestPermissions(MessageActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1001);

                int id = menuItem.getItemId();
                if (id == R.id.action_send_location) {
                    Intent intent = new Intent(MessageActivity.this, PickLocationActivity.class);
                    pickLocationLauncher.launch(intent);
                    return true;

                }
                return false;
            }
        });

        forceShowIcons(popupMenu);

        popupMenu.show();
    }

    private ActivityResultLauncher<Intent> pickLocationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Log.d("result", String.valueOf(result));
                        double latitude = data.getDoubleExtra("lat", 0);
                        double longitude = data.getDoubleExtra("lng", 0);
                        String locationMessage = "location:" + latitude + "," + longitude;
                        sendCordinate(locationMessage);
                    }
                }
            });

    private void forceShowIcons(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_VIDEO_REQUEST && data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    Log.d("MessageActivity", "Selected URI: " + selectedFileUri.toString());
                    try {
                        String fileName = "media_" + System.currentTimeMillis() + getFileExtension(selectedFileUri);
                        File destinationFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                        copyUriToFile(selectedFileUri, destinationFile);

                        Log.d("MessageActivity", "Copied to private storage: " + destinationFile.getAbsolutePath());
                        uploadMediaToCloudinary(destinationFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e("MessageActivity", "Error copying file: " + e.getMessage());
                        Toast.makeText(this, "Error processing selected file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Log.d("MessageActivity", "Selection cancelled or failed. Result code: " + resultCode);
        }
    }

    private String getFileExtension(Uri uri) {
        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(getContentResolver().getType(uri));
        return extension != null ? "." + extension : "";
    }

    private void copyUriToFile(Uri uri, File destination) throws IOException {
        try (java.io.InputStream input = getContentResolver().openInputStream(uri);
             java.io.OutputStream output = new java.io.FileOutputStream(destination)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        }
    }

    interface ChannelCallback {
        void onChannelNameBuilt(String channelName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceMessageManager != null) {
            voiceMessageManager.release();
        }
        stopRecordingTimer();
        if (pendingMediaUri != null) {
            revokeUriPermission(pendingMediaUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturningFromSettings) {
            isReturningFromSettings = false;
            // checkAndRequestCameraPermissions();
        }
    }

    private void showPermissionDeniedDialog() {
        if (!isReturningFromSettings) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Camera and storage permissions are required to take photos. Please enable them in app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        settingsLauncher.launch(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Permissions are still required to use the camera", Toast.LENGTH_LONG).show();
            isReturningFromSettings = false;
        }
    }


    public interface ChannelNameCallback {
        void onChannelNameBuilt(String channelName);
    }

    //Load tin nhắn với bạn trong contact
    private void loadMessages() {
        if (contact != null) {
            String toUserId = contact.getId();
            Log.d("ChatDebug", "To User ID: " + toUserId);
            repo.getCurrentUserId(myUserId -> {
                if (myUserId != null) {
                    String chatId = myUserId.compareTo(toUserId) < 0 ? myUserId + "_" + toUserId : toUserId + "_" + myUserId;
                    DatabaseReference chatRef = mDatabase.child("messages").child(chatId);
                    Log.d("ChatDebug", "Chat ID được tạo: " + chatId);
                    chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    messageList.clear(); // Xóa danh sách cũ

                                    if (snapshot.exists()) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            ChatMessage message = child.getValue(ChatMessage.class);
                                            if (message != null) {
                                                messageList.add(message);
                                            }
                                        }
                                    } else {
                                        // Nếu chưa có tin nhắn, hiển thị tin nhắn mở màn với timestamp hợp lệ
                                        String timeStamp = String.valueOf(System.currentTimeMillis());
                                        messageList.add(new ChatMessage(myUserId, toUserId, "Xin chào! Hãy bắt đầu cuộc trò chuyện nào!", timeStamp));
                                    }
                                    chatAdapter.notifyDataSetChanged(); // Cập nhật danh sách trên giao diện
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.e("MessageActivity", "Lỗi khi tải tin nhắn", error.toException());
                                }
                            });
                } else {
                    Log.e("ChatDebug", "User ID không lấy được!");
                }
            });
        }
    }

    // Thêm các phương thức này vào cuối class, trước dấu } cuối cùng
    private void setupVoiceMessage() {
        voiceMessageManager = new VoiceMessageManager(this);

        voiceMessageManager.setOnRecordingStateChangeListener(isRecording -> {
            runOnUiThread(() -> updateRecordingUI(isRecording));
        });

        btnVoice.setOnClickListener(v -> {
            if (checkRecordPermission()) {
                startRecording();
            } else {
                requestRecordPermission();
            }
        });

        btnCancelRecording.setOnClickListener(v -> cancelRecording());
        btnSendRecording.setOnClickListener(v -> stopRecording());
    }

    private boolean checkRecordPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_PERMISSION_CODE);
    }

    private void startRecording() {
        if (voiceMessageManager.isRecording()) return;

        voiceMessageManager.startRecording();
        showRecordingUI();
        startRecordingTimer();
    }

    private void stopRecording() {
        long recordingDuration = voiceMessageManager.getRecordingDuration();
        if (recordingDuration < recordingMinimumDuration) {
            Toast.makeText(this, "Tin nhắn thoại quá ngắn", Toast.LENGTH_SHORT).show();
            cancelRecording();
            return;
        }

        File recordingFile = voiceMessageManager.stopRecording();
        hideRecordingUI();

        if (recordingFile != null && recordingFile.exists() && recordingFile.length() > 0) {
            uploadVoiceMessage(recordingFile);
        } else {
            Toast.makeText(this, "Lỗi: File ghi âm không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelRecording() {
        voiceMessageManager.cancelRecording();
        hideRecordingUI();
    }

    private void showRecordingUI() {
        inputContainer.setVisibility(View.GONE);
        recordingStateView.setVisibility(View.VISIBLE);

        if (ivRecordingIcon.getDrawable() instanceof AnimatedVectorDrawable) {
            ((AnimatedVectorDrawable) ivRecordingIcon.getDrawable()).start();
        }
    }

    private void hideRecordingUI() {
        inputContainer.setVisibility(View.VISIBLE);
        recordingStateView.setVisibility(View.GONE);
        stopRecordingTimer();
    }

    private void startRecordingTimer() {
        recordingTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            int seconds = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                seconds++;
                int minutes = seconds / 60;
                int remainingSeconds = seconds % 60;
                tvRecordingTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds));
            }

            @Override
            public void onFinish() {}
        };
        recordingTimer.start();
    }

    private void stopRecordingTimer() {
        if (recordingTimer != null) {
            recordingTimer.cancel();
            recordingTimer = null;
        }
    }

    private void updateRecordingUI(boolean isRecording) {
        if (isRecording) {
            showRecordingUI();
        } else {
            hideRecordingUI();
        }
    }
    private void uploadVoiceMessage(File file) {
        if (!file.exists()) return;
        // progressDialog.show();

        CloudinaryHelper.uploadMedia(this, file.getAbsolutePath(), new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String url) {
                runOnUiThread(() -> {
                    // progressDialog.dismiss();
                    if (url != null && !url.isEmpty()) {
                        sendVoiceMessage(url);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // progressDialog.dismiss();
                });
            }
        });
    }


    private void sendVoiceMessage(String url) {
        repo.getCurrentUserId(fromUserId -> {
            if (fromUserId == null || fromUserId.isEmpty()) {
                Toast.makeText(this, "Lỗi: Không lấy được ID người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            String voiceMessage = "voice:" + url;
            repo.sendMessage(fromUserId, toUserId, voiceMessage);
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RECORD_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã được cấp quyền ghi âm", Toast.LENGTH_SHORT).show();
                startRecording();
            } else {
                Toast.makeText(this, "Cần quyền ghi âm để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageVideoPicker();
            } else {
                Toast.makeText(this, "Cần quyền truy cập bộ nhớ để chọn file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}