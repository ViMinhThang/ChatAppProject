package com.project.chatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;

public class PhoneNumberActivity extends AppCompatActivity {

    private CountryCodePicker ccp;
    private EditText etPhoneNumber;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_input);

        // Ánh xạ các view
        ImageView icBack = findViewById(R.id.ic_back);
        ccp = findViewById(R.id.ccp);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnContinue = findViewById(R.id.btn_continue);

        // Đặt quốc gia mặc định là Indonesia (+62)
        ccp.setDefaultCountryUsingNameCode("ID");
        ccp.resetToDefaultCountry();

        // Xử lý khi nhấn nút quay lại
        icBack.setOnClickListener(view -> finish());

        // Xử lý khi nhấn nút Continue
        btnContinue.setOnClickListener(view -> {
            String phoneCode = ccp.getSelectedCountryCodeWithPlus();
            String phoneNumber = etPhoneNumber.getText().toString();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại của bạn", Toast.LENGTH_SHORT).show();
            } else {
                String fullPhoneNumber = phoneCode + " " + phoneNumber;
                Toast.makeText(this, "Số điện thoại: " + fullPhoneNumber, Toast.LENGTH_SHORT).show();
                // Tiếp tục logic xử lý (chuyển màn hình, xác thực...)
            }
        });
    }
}