package com.project.chatapp.screen.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.project.chatapp.R;
import com.project.chatapp.screen.chat.ChatsActivity;

import java.util.concurrent.TimeUnit;

public class OTPVerifyActivity extends AppCompatActivity {
    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private String verificationId;
    private String phoneNumber;
    private boolean isNewUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entercode);
        mAuth = FirebaseAuth.getInstance();
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        String code = createCode();
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        phoneNumber = "+84" + phoneNumber.substring(1);
        isNewUser = getIntent().getBooleanExtra("isNewUser", false);
        sendVerificationCode(phoneNumber);

        otp1.addTextChangedListener(new GenericTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new GenericTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new GenericTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new GenericTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new GenericTextWatcher(otp5, otp6));


        otp6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    String code = createCode();
                    if (code.length() == 6) {
                        if (verificationId != null) {
                            verifyCode(code);
                        } else {
                            Toast.makeText(OTPVerifyActivity.this, "Vui lòng chờ mã xác thực được gửi.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Xử lý quay lại trang trước
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại activity trước đó
            }
        });

        //  Intent intent = new Intent(LoginActivity.this, VerifyOtpActivity.class);
        //  intent.putExtra("phone_number", phoneNumber); // phoneNumber = số người dùng nhập
        //  startActivity(intent);
    }

    public String createCode() {
        return otp1.getText().toString().trim() + otp2.getText().toString().trim() + otp3.getText().toString().trim() + otp4.getText().toString().trim() + otp5.getText().toString().trim() + otp6.getText().toString().trim();
    }

    private class GenericTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        public GenericTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) {
                nextView.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void sendVerificationCode(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance()).setPhoneNumber(phone).setTimeout(120L, TimeUnit.SECONDS).setActivity(this).setCallbacks(callbacks).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            signInWithCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OTPVerifyActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            OTPVerifyActivity.this.verificationId = verificationId;
        }

    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (isNewUser) {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.putExtra("phoneNumber", phoneNumber);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, ChatsActivity.class);
                    intent.putExtra("userPhoneNumber", phoneNumber);
                    startActivity(intent);
                }
                finish();
            } else {
                Toast.makeText(this, "Xác thực thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
