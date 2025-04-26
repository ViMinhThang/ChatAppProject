package com.project.chatapp.screen.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.screen.chat.ChatsActivity;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText firstName, lastName;
    ImageView avatar;
    Button btnRegister;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firstName = findViewById(R.id.firstNameInput);
        lastName = findViewById(R.id.lastNameInput);
        btnRegister = findViewById(R.id.btnRegister);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getIntent().getStringExtra("phoneNumber");
                String first = firstName.getText().toString().trim();
                String last = lastName.getText().toString().trim();
                String fullName = last + " " + first;

                usersRef.orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            // Tìm số lượng user hiện tại
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    long userCount = snapshot.getChildrenCount();
                                    String newUserKey = "user" + (userCount + 1);

                                    Map<String, Object> newUser = new HashMap<>();
                                    newUser.put("userID", newUserKey);
                                    newUser.put("name", fullName);
                                    newUser.put("phone", phoneNumber);
                                    newUser.put("email", "");
                                    newUser.put("profile_picture", "");
                                    newUser.put("status", "offline");
                                    newUser.put("last_login", "");
                                    Map<String, Object> preferences = new HashMap<>();
                                    preferences.put("theme", "light");
                                    preferences.put("notifications", true);
                                    preferences.put("language", "en");
                                    newUser.put("preferences", preferences);

                                    usersRef.child(newUserKey).setValue(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                Intent intent = new Intent(RegisterActivity.this, ChatsActivity.class);
                                                intent.putExtra("userPhoneNumber", phoneNumber);
                                                startActivity(intent);
                                                Toast.makeText(RegisterActivity.this, "User created: " + newUserKey, Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                                            });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(RegisterActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Phone number already exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(RegisterActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}