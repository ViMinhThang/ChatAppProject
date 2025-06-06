package com.project.chatapp.screen.chat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;
import com.project.chatapp.R;

public class ImageViewerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private PhotoView photoView;
    private ProgressBar progressBar;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        photoView = findViewById(R.id.photoView);
        progressBar = findViewById(R.id.progressBar);
        ImageView btnClose = findViewById(R.id.btnClose);
        ImageView btnDownload = findViewById(R.id.btnDownload);

        imageUrl = getIntent().getStringExtra("image_url");
        if (imageUrl != null) {
            loadImage(imageUrl);
        } else {
            Toast.makeText(this, "Error: No image URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnClose.setOnClickListener(v -> finish());
        btnDownload.setOnClickListener(v -> checkPermissionAndDownload());
    }

    private void loadImage(String url) {
        progressBar.setVisibility(View.VISIBLE);
        try {
            Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                            com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                            boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ImageViewerActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                               com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                               com.bumptech.glide.load.DataSource dataSource,
                                               boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(photoView);
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            // Clear any resources
            if (photoView != null) {
                Glide.with(this).clear(photoView);
                photoView.setImageDrawable(null);
            }
        } catch (Exception e) {
            // Ignore any errors during cleanup
        }
        super.onDestroy();
    }

    private void checkPermissionAndDownload() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 and above, we don't need WRITE_EXTERNAL_STORAGE permission
            downloadImage();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            downloadImage();
        }
    }

    private void downloadImage() {
        try {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(imageUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | 
                                         DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("Image")
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                        "ChatApp/image_" + System.currentTimeMillis() + ".jpg");

            downloadManager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 