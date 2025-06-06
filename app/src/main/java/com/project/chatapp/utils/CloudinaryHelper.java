package com.project.chatapp.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    public interface UploadCallback {
        void onSuccess(String url);
        void onError(String error);
    }

    public static void init(Context context) {
        if (!isInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", "dufstukhi");
                config.put("api_key", "373572937183563");
                config.put("api_secret", "CFQbg9iSL-DY5Sv_Dq3lCwGXWIw");
                MediaManager.init(context, config);
                isInitialized = true;
                Log.d(TAG, "Cloudinary initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
            }
        }
    }

    public static void uploadMedia(Context context, String filePath, UploadCallback callback) {
        init(context);
        
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "File does not exist: " + filePath);
            callback.onError("File does not exist");
            return;
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        Log.d(TAG, "File extension: " + extension);
        
        String resourceType = extension != null && 
            (extension.equals("mp4") || extension.equals("mov") || extension.equals("3gp")) 
            ? "video" : "image";
        Log.d(TAG, "Resource type: " + resourceType);

        try {
            MediaManager.get().upload(filePath)
                .option("resource_type", resourceType)
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started. Request ID: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Upload progress: " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("url");
                        Log.d(TAG, "Upload success. URL: " + url);
                        callback.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
        } catch (Exception e) {
            Log.e(TAG, "Error dispatching upload: " + e.getMessage());
            callback.onError("Error starting upload: " + e.getMessage());
        }
    }
} 