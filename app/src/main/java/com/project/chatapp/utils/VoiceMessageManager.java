package com.project.chatapp.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class VoiceMessageManager {
    private MediaRecorder mediaRecorder;
    private boolean isRecording;
    private long recordingStartTime;
    private File recordingFile;
    private OnRecordingStateChangeListener stateChangeListener;
    private final Context context;

    public interface OnRecordingStateChangeListener {
        void onStateChanged(boolean isRecording);
    }

    public VoiceMessageManager(Context context) {
        this.context = context;
        this.isRecording = false;
    }

    public void setOnRecordingStateChangeListener(OnRecordingStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public void startRecording() {
        if (isRecording) return;

        recordingFile = new File(context.getCacheDir(),
                "voice_message_" + System.currentTimeMillis() + ".m4a");

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(recordingFile.getAbsolutePath());

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                recordingStartTime = SystemClock.elapsedRealtime();
                if (stateChangeListener != null) {
                    stateChangeListener.onStateChanged(true);
                }
            } catch (IOException e) {
                Log.e("VoiceMessageManager", "Error preparing MediaRecorder: " + e.getMessage());
                e.printStackTrace();
                releaseMediaRecorder();
                isRecording = false;
                if (stateChangeListener != null) {
                    stateChangeListener.onStateChanged(false);
                }
            }
        } catch (Exception e) {
            Log.e("VoiceMessageManager", "Error setting up MediaRecorder: " + e.getMessage());
            e.printStackTrace();
            releaseMediaRecorder();
            isRecording = false;
            if (stateChangeListener != null) {
                stateChangeListener.onStateChanged(false);
            }
        }
    }

    public File stopRecording() {
        if (!isRecording) return null;

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                releaseMediaRecorder();
            }
        } catch (Exception e) {
            Log.e("VoiceMessageManager", "Error stopping MediaRecorder: " + e.getMessage());
            e.printStackTrace();
        }

        isRecording = false;
        if (stateChangeListener != null) {
            stateChangeListener.onStateChanged(false);
        }

        return recordingFile;
    }

    public void cancelRecording() {
        stopRecording();
        if (recordingFile != null && recordingFile.exists()) {
            recordingFile.delete();
        }
        recordingFile = null;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getRecordingDuration() {
        if (isRecording) {
            return SystemClock.elapsedRealtime() - recordingStartTime;
        }
        return 0;
    }

    public void release() {
        releaseMediaRecorder();
        isRecording = false;
        if (recordingFile != null && recordingFile.exists()) {
            recordingFile.delete();
        }
        recordingFile = null;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e("VoiceMessageManager", "Error releasing MediaRecorder: " + e.getMessage());
                e.printStackTrace();
            }
            mediaRecorder = null;
        }
    }
}
