package com.project.chatapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.chatapp.R;
import com.project.chatapp.model.Contact.ContactModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactSyncManager {
    private static final String TAG = "ContactSyncManager";
    private Context context;
    private DatabaseReference usersRef;
    private DatabaseReference friendsRef;
    private DatabaseReference friendRequestsRef;
    private String currentUserId;
    private OnContactSyncListener listener;

    public interface OnContactSyncListener {
        void onContactSyncComplete(List<ContactModel> registeredContacts, List<ContactModel> unregisteredContacts);
        void onContactSyncFailed(String error);
    }

    public ContactSyncManager(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        friendsRef = database.getReference("friends").child(currentUserId);
        friendRequestsRef = database.getReference("friendRequests");
    }

    public void setOnContactSyncListener(OnContactSyncListener listener) {
        this.listener = listener;
    }

    public void syncContacts() {
        // Đọc danh bạ từ thiết bị
        List<ContactModel> deviceContacts = getDeviceContacts();

        if (deviceContacts.isEmpty()) {
            if (listener != null) {
                listener.onContactSyncFailed("Không tìm thấy liên hệ nào trong danh bạ");
            }
            return;
        }

        // Tạo map số điện thoại để dễ dàng kiểm tra
        final Map<String, ContactModel> phoneContactMap = new HashMap<>();
        for (ContactModel contact : deviceContacts) {
            String normalizedPhone = normalizePhoneNumber(contact.getPhone());
            phoneContactMap.put(normalizedPhone, contact);
        }

        // Lấy danh sách bạn bè hiện tại
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Tạo set chứa ID của bạn bè
                final Set<String> friendIds = new HashSet<>();
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    if (friendId != null) {
                        friendIds.add(friendId);
                    }
                }

                // Lấy danh sách lời mời kết bạn đã gửi
                friendRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot requestsSnapshot) {
                        final Set<String> sentRequestIds = new HashSet<>();

                        for (DataSnapshot userSnapshot : requestsSnapshot.getChildren()) {
                            if (userSnapshot.hasChild(currentUserId)) {
                                sentRequestIds.add(userSnapshot.getKey());
                            }
                        }

                        // Kiểm tra số điện thoại nào đã đăng ký trên Firebase
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                List<ContactModel> registeredContacts = new ArrayList<>();
                                List<ContactModel> unregisteredContacts = new ArrayList<>();

                                // Duyệt qua tất cả người dùng trong Firebase
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String userId = userSnapshot.getKey();
                                    String phone = userSnapshot.child("phone").getValue(String.class);

                                    if (phone != null && userId != null && !userId.equals(currentUserId)) {
                                        String normalizedPhone = normalizePhoneNumber(phone);

                                        // Kiểm tra xem số điện thoại này có trong danh bạ không
                                        if (phoneContactMap.containsKey(normalizedPhone)) {
                                            ContactModel contact = phoneContactMap.get(normalizedPhone);

                                            // Lấy thêm thông tin từ Firebase
                                            String name = userSnapshot.child("name").getValue(String.class);
                                            if (name != null && !name.isEmpty()) {
                                                contact.setName(name);
                                            }

                                            String status = userSnapshot.child("status").getValue(String.class);
                                            if (status != null) {
                                                contact.setStatus(status);
                                            } else {
                                                contact.setStatus("Online");
                                            }

                                            String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);
                                            if (avatarUrl != null) {
                                                contact.setAvatarUrl(avatarUrl);
                                            }

                                            // Lưu userId vào contact để dùng sau này
                                            contact.setUserId(userId);

                                            // Kiểm tra xem đã là bạn bè chưa
                                            if (friendIds.contains(userId)) {
                                                contact.setFriendStatus("Đã là bạn");
                                            } else if (sentRequestIds.contains(userId)) {
                                                contact.setFriendStatus("Đã gửi lời mời");
                                            } else {
                                                contact.setFriendStatus("Kết bạn");
                                            }

                                            registeredContacts.add(contact);

                                            // Xóa khỏi map để không xử lý lại
                                            phoneContactMap.remove(normalizedPhone);
                                        }
                                    }
                                }

                                // Những số còn lại trong map là chưa đăng ký
                                for (ContactModel contact : phoneContactMap.values()) {
                                    contact.setStatus("Chưa sử dụng app");
                                    contact.setFriendStatus("Mời dùng app");
                                    unregisteredContacts.add(contact);
                                }

                                // Thông báo kết quả
                                if (listener != null) {
                                    listener.onContactSyncComplete(registeredContacts, unregisteredContacts);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, "Firebase query cancelled", databaseError.toException());
                                if (listener != null) {
                                    listener.onContactSyncFailed(databaseError.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Firebase query cancelled", databaseError.toException());
                        if (listener != null) {
                            listener.onContactSyncFailed(databaseError.getMessage());
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase query cancelled", databaseError.toException());
                if (listener != null) {
                    listener.onContactSyncFailed(databaseError.getMessage());
                }
            }
        });
    }

    private List<ContactModel> getDeviceContacts() {
        List<ContactModel> contactList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            // Lấy chỉ số cột trước khi duyệt cursor
            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            // Kiểm tra xem có tìm thấy cột không
            if (nameColumnIndex >= 0 && phoneColumnIndex >= 0) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameColumnIndex);
                    String phoneNumber = cursor.getString(phoneColumnIndex);

                    // Kiểm tra dữ liệu hợp lệ
                    if (name != null && phoneNumber != null) {
                        ContactModel contact = new ContactModel(
                                R.drawable.user_info,
                                name,
                                phoneNumber,
                                ""
                        );
                        contactList.add(contact);
                    }
                }
            } else {
                Log.e(TAG, "Column not found: DISPLAY_NAME or NUMBER");
            }
            cursor.close();
        }

        return contactList;
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
