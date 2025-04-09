package com.project.chatapp; // Thay đổi package nếu cần

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnStartMessaging;
    TextView tvTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hello);

        btnStartMessaging = findViewById(R.id.button_start);
        tvTerms = findViewById(R.id.terms_policy);

        btnStartMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình nhập số điện thoại
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