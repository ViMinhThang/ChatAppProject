package com.project.chatapp.screen.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;
import com.project.chatapp.R;

public class PhoneNumberActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText etPhoneNumber;
    private TextView btnContinue;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_input);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các view
        ImageView icBack = findViewById(R.id.ic_back);
        ccp = findViewById(R.id.ccp);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnContinue = findViewById(R.id.btn_continue);

        ccp.setDefaultCountryUsingNameCode("VN");
        ccp.resetToDefaultCountry();

        // Xử lý khi nhấn nút quay lại
        icBack.setOnClickListener(view -> finish());

        // Xử lý khi nhấn nút Continue
        btnContinue.setOnClickListener(view -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                checkUserExist(phoneNumber);
            } else {
                Toast.makeText(this, "Nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserExist(String phoneNumber) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("phone").equalTo("+84" + phoneNumber.substring(1)).get().
                addOnSuccessListener(dataSnapshot -> {
            boolean isNewUser = !dataSnapshot.exists();
            sendOtp(phoneNumber, isNewUser);
        }).addOnFailureListener(e -> {
                    Log.d("Error",e.getMessage());
            Toast.makeText(this, "Lỗi kiểm tra người dùng"+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void sendOtp(String phoneNumber, boolean isNewUser) {
        Intent intent = new Intent(this, OTPVerifyActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("isNewUser", isNewUser);
        startActivity(intent);
    }
}