package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.project.chatapp.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.data.FirebaseMessengerRepository;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class IncomingCallActivity extends AppCompatActivity {
    private String myUserId;
    private String otherUserId;
    private DatabaseReference callRef;
    private Ringtone ringtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        String callerName = getIntent().getStringExtra("callerName");
        String channelName = getIntent().getStringExtra("channelName");
        String fromUserId = getIntent().getStringExtra("fromUserId");
        otherUserId = fromUserId;

        TextView tvCallerName = findViewById(R.id.tvCallerName);
        tvCallerName.setText(callerName);

        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnDecline = findViewById(R.id.btnDecline);

        // Phát chuông khi có cuộc gọi đến
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }

        // Lấy myUserId thực tế từ Firebase trước khi lắng nghe node và xử lý các thao tác
        new FirebaseMessengerRepository().getCurrentUserId(myUserIdReal -> {
            if (myUserIdReal == null || myUserIdReal.isEmpty()) {
                android.widget.Toast.makeText(this, "Không lấy được userId, vui lòng đăng nhập lại!", android.widget.Toast.LENGTH_LONG).show();
                if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
                finish();
                return;
            }
            myUserId = myUserIdReal;
            // Lắng nghe node /calls/{myUserId}
            callRef = FirebaseDatabase.getInstance().getReference().child("calls").child(myUserId);
            callRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Log.d("CALL_DEBUG", "Node /calls/" + myUserId + " đã bị xóa, đóng IncomingCallActivity");
                        if (!isFinishing()) {
                            runOnUiThread(() -> android.widget.Toast.makeText(IncomingCallActivity.this, "Cuộc gọi đã bị hủy!", android.widget.Toast.LENGTH_SHORT).show());
                        }
                        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
                        finish();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });

            btnAccept.setOnClickListener(v -> {
                if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
                // Lấy userId thực tế từ Firebase (đã lấy ở trên)
                Log.d("CALL_DEBUG", "Accept call: channelName=" + channelName + ", myUserId=" + myUserId + ", fromUserId=" + fromUserId);
                if (myUserId == null || myUserId.isEmpty()) {
                    android.widget.Toast.makeText(this, "Không lấy được userId, vui lòng đăng nhập lại!", android.widget.Toast.LENGTH_LONG).show();
                    return;
                }
                // Kiểm tra node còn tồn tại không trước khi accept
                callRef.get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        android.widget.Toast.makeText(this, "Cuộc gọi đã bị hủy hoặc hết hạn!", android.widget.Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Log.d("CALL_DEBUG", "Mở AudioCallActivity: channelName=" + channelName + ", myUserId=" + myUserId + ", fromUserId=" + fromUserId);
                    Intent intent = new Intent(this, AudioCallActivity.class);
                    intent.putExtra("channelName", channelName);
                    intent.putExtra("name", callerName);
                    intent.putExtra("fromUserId", myUserId); // userId thực tế
                    intent.putExtra("toUserId", fromUserId);
                    startActivity(intent);
                    finish();
                });
            });
            btnDecline.setOnClickListener(v -> {
                if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
                // Chỉ xóa node cuộc gọi của callee khi từ chối
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                    .child("calls").child(myUserId).removeValue();
                // Gửi tín hiệu từ chối cho caller
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference()
                    .child("calls_status").child(otherUserId).child("end").setValue(true);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private void endCall() {
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference().child("calls").child(myUserId).removeValue();
    }
} 