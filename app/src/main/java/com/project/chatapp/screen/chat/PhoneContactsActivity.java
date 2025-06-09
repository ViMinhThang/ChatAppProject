package com.project.chatapp.screen.chat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.PhoneContactAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhoneContactsActivity extends AppCompatActivity {
    private static final String TAG = "PhoneContactsActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private ImageView btnBack;
    private EditText edtSearch;
    private RecyclerView rvContacts;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;
    private DatabaseReference friendRequestsRef;
    private String currentUserId;

    // Data
    private List<ContactModel> allContacts;
    private List<ContactModel> registeredContacts;
    private List<ContactModel> filteredContacts;
    private PhoneContactAdapter adapter;
    private Set<String> friendIds;
    private Set<String> sentRequestIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_contacts);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        friendsRef = database.getReference("friends").child(currentUserId);
        friendRequestsRef = database.getReference("friendRequests");

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        rvContacts = findViewById(R.id.rvContacts);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);

        // Initialize data
        allContacts = new ArrayList<>();
        registeredContacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();
        friendIds = new HashSet<>();
        sentRequestIds = new HashSet<>();

        // Setup RecyclerView
        adapter = new PhoneContactAdapter(filteredContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        // Setup click listeners
        setupClickListeners();

        // Setup search
        setupSearch();

        // Check permission and load contacts
        if (hasContactPermission()) {
            loadData();
        } else {
            requestContactPermission();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        adapter.setOnContactActionListener(new PhoneContactAdapter.OnContactActionListener() {
            @Override
            public void onAddFriend(ContactModel contact) {
                sendFriendRequest(contact);
            }

            @Override
            public void onCancelRequest(ContactModel contact) {
                cancelFriendRequest(contact);
            }

            @Override
            public void onInvite(ContactModel contact) {
                inviteToApp(contact);
            }
        });
    }

    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterContacts(s.toString());
            }
        });
    }

    private void filterContacts(String query) {
        filteredContacts.clear();

        if (query.isEmpty()) {
            filteredContacts.addAll(registeredContacts);
        } else {
            String searchQuery = query.toLowerCase();
            for (ContactModel contact : registeredContacts) {
                if (contact.getName().toLowerCase().contains(searchQuery) ||
                        contact.getPhone().contains(searchQuery)) {
                    filteredContacts.add(contact);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredContacts.isEmpty() && !registeredContacts.isEmpty()) {
            txtEmpty.setText("Không tìm thấy kết quả");
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        showLoading(true);

        // Step 1: Load friends
        loadFriends(() -> {
            // Step 2: Load sent requests
            loadSentRequests(() -> {
                // Step 3: Load phone contacts and match with Firebase users
                loadPhoneContacts();
            });
        });
    }

    private void loadFriends(Runnable onComplete) {
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendIds.clear();
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null) {
                        friendIds.add(friendId);
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading friends: " + databaseError.getMessage());
                onComplete.run();
            }
        });
    }

    private void loadSentRequests(Runnable onComplete) {
        friendRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sentRequestIds.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot requestSnapshot : userSnapshot.getChildren()) {
                        if (requestSnapshot.getKey().equals(currentUserId)) {
                            sentRequestIds.add(userSnapshot.getKey());
                            break;
                        }
                    }
                }
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading sent requests: " + databaseError.getMessage());
                onComplete.run();
            }
        });
    }

    private void loadPhoneContacts() {
        // Load phone contacts
        List<ContactModel> deviceContacts = getDeviceContacts();
        if (deviceContacts.isEmpty()) {
            showEmpty(true);
            showLoading(false);
            return;
        }

        // Create map for easy lookup
        Map<String, ContactModel> phoneContactMap = new HashMap<>();
        for (ContactModel contact : deviceContacts) {
            String normalizedPhone = normalizePhoneNumber(contact.getPhone());
            phoneContactMap.put(normalizedPhone, contact);
        }

        // Match with Firebase users
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                registeredContacts.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String phone = userSnapshot.child("phone").getValue(String.class);

                    if (phone != null && !userId.equals(currentUserId)) {
                        String normalizedPhone = normalizePhoneNumber(phone);
                        ContactModel contact = phoneContactMap.get(normalizedPhone);

                        if (contact != null) {
                            // Update with Firebase info
                            String name = userSnapshot.child("name").getValue(String.class);
                            String status = userSnapshot.child("status").getValue(String.class);
                            String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);

                            if (name != null && !name.isEmpty()) {
                                contact.setName(name);
                            }
                            contact.setStatus(status != null ? status : "Online");
                            contact.setAvatarUrl(avatarUrl);
                            contact.setUserId(userId);

                            // Set friend status
                            if (friendIds.contains(userId)) {
                                contact.setFriendStatus("Đã là bạn");
                            } else if (sentRequestIds.contains(userId)) {
                                contact.setFriendStatus("Đã gửi lời mời");
                            } else {
                                contact.setFriendStatus("Kết bạn");
                            }

                            registeredContacts.add(contact);
                        }
                    }
                }

                // Update UI
                updateContactsList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error matching contacts: " + databaseError.getMessage());
                showLoading(false);
                Toast.makeText(PhoneContactsActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<ContactModel> getDeviceContacts() {
        List<ContactModel> deviceContacts = new ArrayList<>();

        try {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            );

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (nameIndex >= 0 && phoneIndex >= 0) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(nameIndex);
                        String phone = cursor.getString(phoneIndex);

                        if (name != null && phone != null) {
                            ContactModel contact = new ContactModel();
                            contact.setName(name);
                            contact.setPhone(phone);
                            contact.setAvatar(R.drawable.default_avatar);
                            deviceContacts.add(contact);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return deviceContacts;
    }

    private void updateContactsList() {
        filteredContacts.clear();
        filteredContacts.addAll(registeredContacts);
        adapter.notifyDataSetChanged();

        showLoading(false);
        showEmpty(registeredContacts.isEmpty());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvContacts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        txtEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvContacts.setVisibility(show ? View.GONE : View.VISIBLE);

        if (show) {
            txtEmpty.setText("Không tìm thấy liên hệ nào đang dùng ứng dụng");
        }
    }

    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData();
            } else {
                Toast.makeText(this, "Cần quyền truy cập danh bạ để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void sendFriendRequest(ContactModel contact) {
        String targetUserId = contact.getUserId();
        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Không thể gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        friendRequestsRef.child(targetUserId).child(currentUserId).setValue("pending")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    contact.setFriendStatus("Đã gửi lời mời");
                    sentRequestIds.add(targetUserId);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelFriendRequest(ContactModel contact) {
        String targetUserId = contact.getUserId();
        if (targetUserId == null || targetUserId.isEmpty()) {
            return;
        }

        friendRequestsRef.child(targetUserId).child(currentUserId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    contact.setFriendStatus("Kết bạn");
                    sentRequestIds.remove(targetUserId);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void inviteToApp(ContactModel contact) {
        String message = "Hãy tham gia cùng tôi trên ứng dụng chat: https://play.google.com/store/apps/details?id=com.project.chatapp";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Lời mời sử dụng ứng dụng chat");
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Mời bạn bè"));
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
