package com.project.chatapp.screen.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.model.Contact.FriendRequestAdapter;
import com.project.chatapp.model.Contact.FriendRequestModel;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestActionListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private List<FriendRequestModel> requestList;
    private FriendRequestAdapter adapter;
    private String currentUserId;
    private DatabaseReference friendRequestsRef;
    private DatabaseReference friendsRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lời mời kết bạn");
        toolbar.setNavigationOnClickListener(v -> finish());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        friendRequestsRef = database.getReference("friendRequests").child(currentUserId);
        friendsRef = database.getReference("friends");
        usersRef = database.getReference("users");

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        requestList = new ArrayList<>();
        adapter = new FriendRequestAdapter(requestList);
        adapter.setOnFriendRequestActionListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadFriendRequests();
    }

    private void loadFriendRequests() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        friendRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();

                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String senderId = requestSnapshot.getKey();
                    String status = requestSnapshot.getValue(String.class);

                    if (senderId != null && "pending".equals(status)) {
                        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String name = userSnapshot.child("name").getValue(String.class);
                                String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);
                                String status = userSnapshot.child("status").getValue(String.class);

                                if (name != null) {
                                    FriendRequestModel request = new FriendRequestModel(
                                            senderId,
                                            name,
                                            avatarUrl,
                                            status != null ? status : "Online",
                                            requestSnapshot.child("timestamp").getValue(String.class)
                                    );
                                    requestList.add(request);
                                    updateRequestList();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(FriendRequestsActivity.this,
                                        "Lỗi: " + databaseError.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setText("Không có lời mời kết bạn nào");
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FriendRequestsActivity.this,
                        "Lỗi: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRequestList() {
        progressBar.setVisibility(View.GONE);

        if (requestList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setText("Không có lời mời kết bạn nào");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAcceptRequest(FriendRequestModel request) {
        String senderId = request.getUserId();

        friendsRef.child(currentUserId).child(senderId).setValue(true);

        friendsRef.child(senderId).child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    friendRequestsRef.child(senderId).removeValue();

                    Toast.makeText(this, "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();

                    setResult(Activity.RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRejectRequest(FriendRequestModel request) {
        friendRequestsRef.child(request.getUserId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã từ chối lời mời kết bạn", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
