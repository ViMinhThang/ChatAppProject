package com.project.chatapp.screen.authentication; // Thay đổi package nếu cần

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.project.chatapp.R;

public class MainActivity extends AppCompatActivity {

    FrameLayout btnStartMessaging;
    TextView tvTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hello);
        FirebaseApp.initializeApp(this);
        if (FirebaseApp.getApps(this).size() == 0) {
            Log.d("FirebaseTest", "Firebase Not initialized");
        } else {
            Log.d("FirebaseTest", "Firebase Initialized Sucessfully");
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        btnStartMessaging = findViewById(R.id.button_start);
        tvTerms = findViewById(R.id.terms_priva);

        btnStartMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
                startActivity(intent);
            }
        });

        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Xử lý khi người dùng nhấn vào link Điều khoản & Chính sách
            }
        });
    }
}