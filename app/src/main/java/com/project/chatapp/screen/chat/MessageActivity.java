package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.adapter.ChatApdater;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.model.Chat.ChatsModel;
import com.project.chatapp.model.ChatMessage;
import com.project.chatapp.utils.TimeUtils;
import com.project.chatapp.utils.CloudinaryHelper;
import android.app.ProgressDialog;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.webkit.MimeTypeMap;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.os.Build;
import android.provider.Settings;
import android.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatApdater chatApdater;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageView btnSend, btnBack, btnCamera, btnSendImg;
    private FirebaseMessengerRepository repo;
    private String toUserId;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int PICK_IMAGE_VIDEO_REQUEST = 102;
    private String currentPhotoPath;
    private ProgressDialog progressDialog;
    private static final int STORAGE_PERMISSION_CODE = 103;
    private Uri pendingMediaUri;
    private Uri photoUri;
    private static final String[] CAMERA_PERMISSIONS = {
        Manifest.permission.CAMERA
    };
    
    private static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] STORAGE_PERMISSIONS_33 = {
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    };

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;
    private boolean isReturningFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activiy_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toUserId = getIntent().getStringExtra("userId");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.back_chat);
        btnCamera = findViewById(R.id.btnCammera);
        btnSendImg = findViewById(R.id.btnSendImg);
        messageList = new ArrayList<>();
        chatApdater = new ChatApdater(messageList);
        recyclerView.setAdapter(chatApdater);
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatsActivity.class));
        });
        btnCamera.setOnClickListener(v -> checkAndRequestCameraPermissions());
        btnSendImg.setOnClickListener(v -> checkStoragePermission());
        repo = new FirebaseMessengerRepository();

        repo.getCurrentUserId(userId -> {
            Log.d("UserID", "My ID: " + userId);

            repo.listenForMessages(userId, toUserId, (from, to, message, timestamp) -> {
                boolean isSentByMe = from.equals(userId);
                ChatMessage chatMessage = new ChatMessage(from, to, message, timestamp);
                chatMessage.setSender(isSentByMe);
                messageList.add(chatMessage);
                chatApdater.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            });
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading media...");
        progressDialog.setCancelable(false);

        setupPermissionLaunchers();
    }

    private void setupPermissionLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean allGranted = true;
                for (Boolean isGranted : result.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    startCamera();
                } else {
                    showPermissionDeniedDialog();
                }
            }
        );

        settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                isReturningFromSettings = true;
                checkAndRequestCameraPermissions();
            }
        );
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, text);
            etMessage.setText("");
        });
    }

    private void checkAndRequestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                return;
            }
            startCamera();
        } else {
            // For Android 12 and below
            List<String> permissionsNeeded = new ArrayList<>();
            
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!permissionsNeeded.isEmpty()) {
                requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
                return;
            }
            startCamera();
        }
    }

    private void showPermissionDeniedDialog() {
        if (!isReturningFromSettings) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Camera and storage permissions are required to take photos. Please enable them in app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    settingsLauncher.launch(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
        } else {
            Toast.makeText(this, "Permissions are still required to use the camera", Toast.LENGTH_LONG).show();
            isReturningFromSettings = false;
        }
    }

    private void startCamera() {
        try {
            // Create a file to store the image
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.project.chatapp.fileprovider",
                        photoFile);

                // Create camera intent
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                // Grant URI permissions
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | 
                                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Check if there's a camera app available
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("MessageActivity", "Error starting camera: " + e.getMessage());
            Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e("MessageActivity", "Failed to create directory");
                throw new IOException("Failed to create directory");
            }
        }
        
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                        },
                        STORAGE_PERMISSION_CODE);
            } else {
                openImageVideoPicker();
            }
        } else {
            // For Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                openImageVideoPicker();
            }
        }
    }

    private void openImageVideoPicker() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select Image or Video"), PICK_IMAGE_VIDEO_REQUEST);
        } catch (Exception e) {
            Log.e("MessageActivity", "Error opening picker: " + e.getMessage());
            Toast.makeText(this, "Error opening media picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (currentPhotoPath != null) {
                    try {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        File f = new File(currentPhotoPath);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        this.sendBroadcast(mediaScanIntent);

                        Log.d("MessageActivity", "Starting upload of camera photo: " + currentPhotoPath);
                        uploadMediaToCloudinary(currentPhotoPath);
                    } catch (Exception e) {
                        Log.e("MessageActivity", "Error processing camera photo", e);
                        Toast.makeText(this, "Error processing photo", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == PICK_IMAGE_VIDEO_REQUEST && data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    Log.d("MessageActivity", "Selected URI: " + selectedFileUri.toString());
                    try {
                        String fileName = "media_" + System.currentTimeMillis() + getFileExtension(selectedFileUri);
                        File destinationFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
                        copyUriToFile(selectedFileUri, destinationFile);
                        
                        Log.d("MessageActivity", "Copied to private storage: " + destinationFile.getAbsolutePath());
                        uploadMediaToCloudinary(destinationFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e("MessageActivity", "Error copying file: " + e.getMessage());
                        Toast.makeText(this, "Error processing selected file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            if (requestCode == CAMERA_REQUEST_CODE && currentPhotoPath != null) {
                File file = new File(currentPhotoPath);
                if (file.exists()) {
                    file.delete();
                }
            }
            Log.d("MessageActivity", "Selection cancelled or failed. Result code: " + resultCode);
        }
    }

    private String getFileExtension(Uri uri) {
        String extension = MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(getContentResolver().getType(uri));
        return extension != null ? "." + extension : "";
    }

    private void copyUriToFile(Uri uri, File destination) throws IOException {
        try (java.io.InputStream input = getContentResolver().openInputStream(uri);
             java.io.OutputStream output = new java.io.FileOutputStream(destination)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        }
    }

    private void uploadMediaToCloudinary(String filePath) {
        progressDialog.show();
        Log.d("MessageActivity", "Starting upload to Cloudinary: " + filePath);
        
        CloudinaryHelper.uploadMedia(this, filePath, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String url) {
                Log.d("MessageActivity", "Upload success. URL: " + url);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    sendMediaMessage(url);
                });
            }

            @Override
            public void onError(String error) {
                Log.e("MessageActivity", "Upload error: " + error);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(MessageActivity.this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void sendMediaMessage(String mediaUrl) {
        Log.d("MessageActivity", "Sending media message: " + mediaUrl);
        repo.getCurrentUserId(fromUserId -> {
            repo.sendMessage(fromUserId, toUserId, mediaUrl);
            Log.d("MessageActivity", "Media message sent from " + fromUserId + " to " + toUserId);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any temporary files and permissions
        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                file.delete();
            }
        }
        if (photoUri != null) {
            revokeUriPermission(photoUri, 
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isReturningFromSettings) {
            isReturningFromSettings = false;
            checkAndRequestCameraPermissions();
        }
    }
}