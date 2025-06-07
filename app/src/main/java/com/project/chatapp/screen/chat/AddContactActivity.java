package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.project.chatapp.R;
import com.project.chatapp.databinding.ActivityAddContactBinding;
import com.project.chatapp.model.Contact.ContactModel;

import java.io.Serializable;

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private int avatarResource = R.drawable.change_avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener imagePickerListener = v -> openGallery();
        binding.imgAvatar.setOnClickListener(imagePickerListener);
        binding.btnChangePhoto.setOnClickListener(imagePickerListener);

        View.OnClickListener saveListener = v -> saveContact();
        binding.btnSave.setOnClickListener(saveListener);
        binding.saveContactButton.setOnClickListener(saveListener);
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            binding.imgAvatar.setImageURI(imageUri);

        }
    }

    private void saveContact() {
        String name = binding.contactName.getText().toString().trim();
        String phoneStr = binding.contactPhone.getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Vui lòng nhập tên liên hệ");
            return;
        } else {
            binding.nameInputLayout.setError(null);
        }

        if (phoneStr.isEmpty()) {
            binding.phoneInputLayout.setError("Vui lòng nhập số điện thoại");
            return;
        } else {
            binding.phoneInputLayout.setError(null);
        }

        try {
            int phone = Integer.parseInt(phoneStr);

            ContactModel newContact = new ContactModel(avatarResource, name, phone, "Online"
            );

            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_contact", (Serializable) newContact);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Đã thêm liên hệ thành công", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            binding.phoneInputLayout.setError("Số điện thoại không hợp lệ");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
