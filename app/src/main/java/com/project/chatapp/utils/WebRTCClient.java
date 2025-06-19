package com.project.chatapp.utils;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCClient {
    private static final String TAG = "WebRTCClient";
    private static final String LOCAL_TRACK_ID = "local_track";
    
    private final PeerConnectionFactory peerConnectionFactory;
    private final List<PeerConnection.IceServer> iceServers;
    private PeerConnection peerConnection;
    private AudioTrack localAudioTrack;
    private VideoTrack localVideoTrack;
    private VideoCapturer videoCapturer;
    private final Context context;
    private final WebRTCClientCallback callback;
    private final EglBase eglBase;
    private VideoSource videoSource;
    private SurfaceTextureHelper surfaceTextureHelper;
    private List<RtpSender> senders = new ArrayList<>();
    private boolean isDisposed = false;
    private final String callType;
    private String currentCameraDevice = null;
    private boolean isFrontCamera = true;

    public interface WebRTCClientCallback {
        void onIceCandidateReceived(IceCandidate iceCandidate);
        void onConnectionStateChanged(PeerConnection.PeerConnectionState state);
        void onLocalVideoTrackCreated(VideoTrack videoTrack);
        void onRemoteVideoTrackReceived(VideoTrack videoTrack);
    }

    public WebRTCClient(Context context, WebRTCClientCallback callback, String callType) {
        this.context = context;
        this.callback = callback;
        this.callType = callType;
        this.eglBase = EglBase.create();
        
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();

        // Configure ICE servers
        iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
    }

    public EglBase.Context getEglBaseContext() {
        return eglBase.getEglBaseContext();
    }

    private void createVideoTrack() {
        Log.d(TAG, "createVideoTrack CALLED, this side will send video");
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoSource = peerConnectionFactory.createVideoSource(false);
        videoCapturer = createVideoCapturer();
        
        if (videoCapturer != null) {
            videoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            videoCapturer.startCapture(1280, 720, 30);
            localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID + "_video", videoSource);
            localVideoTrack.setEnabled(true);
            callback.onLocalVideoTrackCreated(localVideoTrack);
            
            // Add video track to peer connection
            if (peerConnection != null) {
                RtpSender sender = peerConnection.addTrack(localVideoTrack);
                if (sender != null) {
                    senders.add(sender);
                }
            }
        }
    }

    private VideoCapturer createVideoCapturer() {
        CameraEnumerator enumerator = new Camera2Enumerator(context);
        String[] deviceNames = enumerator.getDeviceNames();
        // Try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    currentCameraDevice = deviceName;
                    isFrontCamera = true;
                    return videoCapturer;
                }
            }
        }
        // Front facing camera not found, try back camera
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    currentCameraDevice = deviceName;
                    isFrontCamera = false;
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private void createAudioTrack() {
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource);
        localAudioTrack.setEnabled(true);
        
        // Add audio track to peer connection
        if (peerConnection != null) {
            RtpSender sender = peerConnection.addTrack(localAudioTrack);
            if (sender != null) {
                senders.add(sender);
            }
        }
    }

    public void initializePeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        rtcConfig.enableCpuOveruseDetection = true;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        
        // Create PeerConnection with callbacks
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                callback.onIceCandidateReceived(iceCandidate);
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                callback.onConnectionStateChanged(newState);
            }

            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
            }

            @Override
            public void onIceConnectionReceivingChange(boolean receiving) {
                Log.d(TAG, "ICE connection receiving changed: " + receiving);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: " + iceGatheringState);
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.getId());
                for (VideoTrack track : mediaStream.videoTracks) {
                    callback.onRemoteVideoTrackReceived(track);
                }
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                Log.d(TAG, "onAddTrack");
                if (rtpReceiver.track() != null) {
                    if (rtpReceiver.track() instanceof VideoTrack) {
                        callback.onRemoteVideoTrackReceived((VideoTrack) rtpReceiver.track());
                    }
                }
            }
        });

        // Chỉ tạo video track nếu là video call
        if ("video".equalsIgnoreCase(callType)) {
            createVideoTrack();
        }
        // Luôn tạo audio track
        createAudioTrack();
    }

    public void createOffer() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d(TAG, "Local SDP set successfully");
            }

            @Override
            public void onCreateFailure(String s) {
                Log.e(TAG, "Create offer failed: " + s);
            }
        }, constraints);
    }

    public void createAnswer() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.d(TAG, "Local SDP set successfully for answer");
            }

            @Override
            public void onCreateFailure(String s) {
                Log.e(TAG, "Create answer failed: " + s);
            }
        }, constraints);
    }

    public void setRemoteDescription(SessionDescription sessionDescription) {
        peerConnection.setRemoteDescription(new SimpleSdpObserver(), sessionDescription);
    }

    public void addIceCandidate(IceCandidate iceCandidate) {
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void switchCamera() {
        if (videoCapturer instanceof CameraVideoCapturer) {
            CameraVideoCapturer cameraCapturer = (CameraVideoCapturer) videoCapturer;
            cameraCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean isFrontCameraNow) {
                    isFrontCamera = isFrontCameraNow;
                    Log.d(TAG, "Camera switched. Now front: " + isFrontCamera);
                }
                @Override
                public void onCameraSwitchError(String errorDescription) {
                    Log.e(TAG, "Camera switch error: " + errorDescription);
                }
            });
        }
    }

    public void enableVideo(boolean enable) {
        if (localVideoTrack != null) {
            localVideoTrack.setEnabled(enable);
        }
    }

    public void enableAudio(boolean enable) {
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(enable);
        }
    }

    public void release() {
        if (isDisposed) return;
        isDisposed = true;
        Log.d(TAG, "Releasing WebRTC resources");
        
        try {
            if (videoCapturer != null) {
                try {
                    videoCapturer.stopCapture();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Failed to stop video capture", e);
                }
                videoCapturer.dispose();
                videoCapturer = null;
            }

            if (videoSource != null) {
                videoSource.dispose();
                videoSource = null;
            }

            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }

            if (localVideoTrack != null) {
                localVideoTrack.dispose();
                localVideoTrack = null;
            }

            if (localAudioTrack != null) {
                localAudioTrack.dispose();
                localAudioTrack = null;
            }

            // Remove all tracks before closing connection
            for (RtpSender sender : senders) {
                if (peerConnection != null) {
                    peerConnection.removeTrack(sender);
                }
            }
            senders.clear();

            if (peerConnection != null) {
                peerConnection.dispose();
                peerConnection = null;
            }

            if (eglBase != null) {
                eglBase.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during resource release", e);
        }
    }
} 