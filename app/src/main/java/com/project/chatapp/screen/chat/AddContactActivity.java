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

public class AddContactActivity extends AppCompatActivity {

    private ActivityAddContactBinding binding;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        View.OnClickListener imagePickerListener = v -> openGallery();
        binding.imgAvatar.setOnClickListener(imagePickerListener);
        binding.btnChangePhoto.setOnClickListener(imagePickerListener);

        binding.btnSave.setOnClickListener(v -> saveContact());
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
        String phone = binding.contactPhone.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        ContactModel newContact = new ContactModel(
                R.drawable.user_info,
                name,
                phone,
                "Online"
        );

        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_contact", newContact);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
