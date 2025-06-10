package com.project.chatapp.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class VoiceMessageManager {
    private static final String TAG = "VoiceMessageManager";
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String audioFilePath;
    private Context context;
    private long startTime;
    private OnRecordingStateChangeListener stateChangeListener;

    public interface OnRecordingStateChangeListener {
        void onStateChanged(boolean isRecording);
    }

    public VoiceMessageManager(Context context) {
        this.context = context;
    }

    public void setOnRecordingStateChangeListener(OnRecordingStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public boolean startRecording() {
        try {
            // Tạo thư mục lưu file
            File audioDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "VoiceMessages");
            if (!audioDir.exists()) {
                boolean created = audioDir.mkdirs();
                if (!created) {
                    Log.e(TAG, "Failed to create directory: " + audioDir.getAbsolutePath());
                    return false;
                }
            }

            String fileName = "voice_" + System.currentTimeMillis() + ".m4a";
            audioFilePath = new File(audioDir, fileName).getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(audioFilePath);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            startTime = System.currentTimeMillis();

            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(true);
            }

            Log.d(TAG, "Recording started successfully: " + audioFilePath);
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during recording start: " + e.getMessage());
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
    }

    public File stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            Log.w(TAG, "No active recording to stop");
            return null;
        }

        try {
            mediaRecorder.stop();
            releaseMediaRecorder();

            isRecording = false;

            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(false);
            }

            File audioFile = new File(audioFilePath);
            if (audioFile.exists() && audioFile.length() > 0) {
                Log.d(TAG, "Recording stopped successfully: " + audioFilePath);
                Log.d(TAG, "File size: " + audioFile.length() + " bytes");
                return audioFile;
            } else {
                Log.e(TAG, "Recording file is empty or doesn't exist");
                return null;
            }

        } catch (RuntimeException e) {
            Log.e(TAG, "Error stopping recording: " + e.getMessage());
            e.printStackTrace();
            releaseMediaRecorder();
            isRecording = false;
            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(false);
            }
            return null;
        }
    }

    public void cancelRecording() {
        Log.d(TAG, "Cancelling recording");

        if (mediaRecorder != null) {
            try {
                if (isRecording) {
                    mediaRecorder.stop();
                }
            } catch (RuntimeException e) {
                Log.e(TAG, "Error stopping recorder during cancel: " + e.getMessage());
            }
            releaseMediaRecorder();
        }

        if (audioFilePath != null) {
            File file = new File(audioFilePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d(TAG, "Recording file deleted successfully");
                } else {
                    Log.w(TAG, "Failed to delete recording file");
                }
            }
        }

        isRecording = false;
        audioFilePath = null;

        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(false);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getRecordingDuration() {
        if (isRecording && startTime > 0) {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    public String getCurrentRecordingPath() {
        return audioFilePath;
    }

    public void release() {
        Log.d(TAG, "Releasing VoiceMessageManager");

        releaseMediaRecorder();

        if (isRecording && audioFilePath != null) {
            File file = new File(audioFilePath);
            if (file.exists()) {
                file.delete();
            }
        }

        isRecording = false;
        audioFilePath = null;
        stateChangeListener = null;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
                Log.d(TAG, "MediaRecorder released");
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaRecorder: " + e.getMessage());
            }
            mediaRecorder = null;
        }
    }

    public boolean isReady() {
        return !isRecording && mediaRecorder == null;
    }

    public long getCurrentFileSize() {
        if (audioFilePath != null) {
            File file = new File(audioFilePath);
            if (file.exists()) {
                return file.length();
            }
        }
        return 0;
    }

    public String getFormattedDuration() {
        long duration = getRecordingDuration();
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


}
