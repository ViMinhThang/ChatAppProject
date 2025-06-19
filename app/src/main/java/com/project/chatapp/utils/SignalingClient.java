package com.project.chatapp.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public class SignalingClient {
    private final DatabaseReference callRef;
    private final String callId;
    private final String currentUserId;
    private final String remoteUserId;
    private final SignalingCallback callback;

    public interface SignalingCallback {
        void onOfferReceived(SessionDescription offer);
        void onAnswerReceived(SessionDescription answer);
        void onIceCandidateReceived(IceCandidate iceCandidate);
    }

    public SignalingClient(String callId, String currentUserId, String remoteUserId, SignalingCallback callback) {
        this.callId = callId;
        this.currentUserId = currentUserId;
        this.remoteUserId = remoteUserId;
        this.callback = callback;
        
        // Khởi tạo reference đến node cuộc gọi trong Firebase
        callRef = FirebaseDatabase.getInstance().getReference()
                .child("calls").child(callId);
        
        // Lắng nghe các sự kiện signaling
        setupSignalingListeners();
    }

    private void setupSignalingListeners() {
        // Lắng nghe offer/answer
        callRef.child("sdp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue(String.class);
                    String sdp = snapshot.child("sdp").getValue(String.class);
                    String from = snapshot.child("from").getValue(String.class);

                    if (from != null && !from.equals(currentUserId)) {
                        SessionDescription sessionDescription = new SessionDescription(
                                SessionDescription.Type.fromCanonicalForm(type),
                                sdp
                        );

                        if ("offer".equals(type)) {
                            callback.onOfferReceived(sessionDescription);
                        } else if ("answer".equals(type)) {
                            callback.onAnswerReceived(sessionDescription);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        // Lắng nghe ICE candidates
        callRef.child("candidates").child(remoteUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot candidateSnapshot : snapshot.getChildren()) {
                        IceCandidate candidate = new IceCandidate(
                                candidateSnapshot.child("sdpMid").getValue(String.class),
                                candidateSnapshot.child("sdpMLineIndex").getValue(Integer.class),
                                candidateSnapshot.child("sdp").getValue(String.class)
                        );
                        callback.onIceCandidateReceived(candidate);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    public void sendOffer(SessionDescription offer) {
        callRef.child("sdp").setValue(new SignalingMessage(
                "offer",
                offer.description,
                currentUserId
        ));
    }

    public void sendAnswer(SessionDescription answer) {
        callRef.child("sdp").setValue(new SignalingMessage(
                "answer",
                answer.description,
                currentUserId
        ));
    }

    public void sendIceCandidate(IceCandidate candidate) {
        DatabaseReference candidateRef = callRef.child("candidates")
                .child(currentUserId)
                .push();

        candidateRef.setValue(new IceCandidateMessage(
                candidate.sdpMid,
                candidate.sdpMLineIndex,
                candidate.sdp
        ));
    }

    public void sendCallEndSignal() {
        if (callRef != null) {
            callRef.child("status").setValue("ended");
        }
    }

    private static class SignalingMessage {
        public String type;
        public String sdp;
        public String from;

        public SignalingMessage(String type, String sdp, String from) {
            this.type = type;
            this.sdp = sdp;
            this.from = from;
        }
    }

    private static class IceCandidateMessage {
        public String sdpMid;
        public int sdpMLineIndex;
        public String sdp;

        public IceCandidateMessage(String sdpMid, int sdpMLineIndex, String sdp) {
            this.sdpMid = sdpMid;
            this.sdpMLineIndex = sdpMLineIndex;
            this.sdp = sdp;
        }
    }
} 