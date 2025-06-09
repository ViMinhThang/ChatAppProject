package com.project.chatapp.screen.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import com.project.chatapp.model.Contact.FriendAdapter;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private static final String TAG = "ContactFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Views
    private View btnPhoneContacts;
    private View btnFriendRequests;
    private View btnAddContact;
    private EditText edtSearch;
    private RecyclerView rvFriends;
    private ProgressBar progressBar;
    private TextView txtEmpty;
    private TextView txtRequestCount;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;
    private DatabaseReference friendRequestsRef;
    private String currentUserId;
    private ValueEventListener friendsListener;
    private ValueEventListener requestsListener;

    // Data
    private List<ContactModel> friendsList;
    private List<ContactModel> filteredList;
    private FriendAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        friendsRef = database.getReference("friends").child(currentUserId);
        friendRequestsRef = database.getReference("friendRequests").child(currentUserId);

        // Initialize views
        btnPhoneContacts = view.findViewById(R.id.btnPhoneContacts);
        btnFriendRequests = view.findViewById(R.id.btnFriendRequests);
        btnAddContact = view.findViewById(R.id.btnAddContact);
        edtSearch = view.findViewById(R.id.edtSearch);
        rvFriends = view.findViewById(R.id.rvFriends);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        txtRequestCount = view.findViewById(R.id.txtRequestCount);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize data
        friendsList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new FriendAdapter(filteredList);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(adapter);

        // Setup click listeners
        setupClickListeners();

        // Setup search
        setupSearch();

        // Load friends
        loadFriends();

        // Check friend requests
        checkFriendRequests();
    }

    private void setupClickListeners() {
        // Phone contacts button
        btnPhoneContacts.setOnClickListener(v -> {
            if (hasContactPermission()) {
                openPhoneContacts();
            } else {
                requestContactPermission();
            }
        });

        // Friend requests button
        btnFriendRequests.setOnClickListener(v -> {
            openFriendRequests();
        });

        // Add contact button
        btnAddContact.setOnClickListener(v -> {
            openAddContact();
        });

        // Friend item click
        adapter.setOnFriendClickListener(new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(ContactModel friend) {
                openFriendProfile(friend);
            }

            @Override
            public void onCallClick(ContactModel friend) {
                callFriend(friend);
            }

            @Override
            public void onVideoCallClick(ContactModel friend) {
                videoCallFriend(friend);
            }

            @Override
            public void onMessageClick(ContactModel friend) {
                messageFriend(friend);
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
                filterFriends(s.toString());
            }
        });
    }

    private void filterFriends(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(friendsList);
        } else {
            String searchQuery = query.toLowerCase();
            for (ContactModel friend : friendsList) {
                if (friend.getName().toLowerCase().contains(searchQuery) ||
                        friend.getPhone().contains(searchQuery)) {
                    filteredList.add(friend);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty() && !friendsList.isEmpty()) {
            txtEmpty.setText("Không tìm thấy kết quả");
            txtEmpty.setVisibility(View.VISIBLE);
        } else {
            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void loadFriends() {
        showLoading(true);

        friendsListener = friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsList.clear();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null) {
                        loadFriendDetails(friendId);
                    }
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    showEmpty(true);
                    showLoading(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading friends: " + databaseError.getMessage());
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriendDetails(String friendId) {
        usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String status = dataSnapshot.child("status").getValue(String.class);
                    String avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);

                    if (name != null && phone != null) {
                        ContactModel friend = new ContactModel();
                        friend.setUserId(dataSnapshot.getKey());
                        friend.setName(name);
                        friend.setPhone(phone);
                        friend.setStatus(status != null ? status : "Online");
                        friend.setAvatarUrl(avatarUrl);
                        friend.setFriendStatus("Đã là bạn");

                        friendsList.add(friend);

                        // Update UI
                        updateFriendsList();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading friend details: " + databaseError.getMessage());
            }
        });
    }

    private void updateFriendsList() {
        filteredList.clear();
        filteredList.addAll(friendsList);
        adapter.notifyDataSetChanged();

        showLoading(false);
        showEmpty(friendsList.isEmpty());
    }

    private void checkFriendRequests() {
        requestsListener = friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                if (count > 0) {
                    txtRequestCount.setText(String.valueOf(count));
                    txtRequestCount.setVisibility(View.VISIBLE);
                } else {
                    txtRequestCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking friend requests: " + databaseError.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvFriends.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        txtEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvFriends.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean hasContactPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
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
                openPhoneContacts();
            } else {
                Toast.makeText(getContext(), "Cần quyền truy cập danh bạ để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPhoneContacts() {
        Intent intent = new Intent(getActivity(), PhoneContactsActivity.class);
        startActivity(intent);
    }

    private void openFriendRequests() {
        Intent intent = new Intent(getActivity(), FriendRequestsActivity.class);
        startActivity(intent);
    }

    private void openAddContact() {
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        startActivity(intent);
    }

    private void openFriendProfile(ContactModel friend) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra("userId", friend.getUserId());
        startActivity(intent);
    }

    private void callFriend(ContactModel friend) {
        // Implement call functionality
        Toast.makeText(getContext(), "Gọi điện cho " + friend.getName(), Toast.LENGTH_SHORT).show();
    }

    private void videoCallFriend(ContactModel friend) {
        // Implement video call functionality
        Toast.makeText(getContext(), "Gọi video cho " + friend.getName(), Toast.LENGTH_SHORT).show();
    }

    private void messageFriend(ContactModel friend) {
        Intent intent = new Intent(getActivity(), ChatsActivity.class);
        intent.putExtra("userId", friend.getUserId());
        intent.putExtra("userName", friend.getName());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (friendsListener != null) {
            friendsRef.removeEventListener(friendsListener);
        }
        if (requestsListener != null) {
            friendRequestsRef.removeEventListener(requestsListener);
        }
    }
}
