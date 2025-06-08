//package com.project.chatapp.utils;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.database.Cursor;
//import android.provider.ContactsContract;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.project.chatapp.R;
//import com.project.chatapp.model.Contact.contact.addContactModel;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ContactSyncManager {
//    private static final String TAG = "ContactSyncManager";
//    private Context context;
//    private DatabaseReference usersRef;
//    private OnContactSyncListener listener;
//
//    public interface OnContactSyncListener {
//        void onContactSyncComplete(List<addContactModel> registeredContacts, List<addContactModel> unregisteredContacts);
//        void onContactSyncFailed(String error);
//    }
//
//    public ContactSyncManager(Context context) {
//        this.context = context;
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        usersRef = database.getReference("users");
//    }
//
//    public void setOnContactSyncListener(OnContactSyncListener listener) {
//        this.listener = listener;
//    }
//
//    public void syncContacts() {
//        List<addContactModel> deviceContacts = getDeviceContacts();
//
//        if (deviceContacts.isEmpty()) {
//            if (listener != null) {
//                listener.onContactSyncFailed("No contacts found on device");
//            }
//            return;
//        }
//
//        final Map<String, addContactModel> phoneContactMap = new HashMap<>();
//        for (addContactModel contact : deviceContacts) {
//            String normalizedPhone = normalizePhoneNumber(contact.getPhone());
//            phoneContactMap.put(normalizedPhone, contact);
//        }
//
//        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                List<addContactModel> registeredContacts = new ArrayList<>();
//                List<addContactModel> unregisteredContacts = new ArrayList<>();
//
//                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                    String phone = userSnapshot.child("phone").getValue(String.class);
//                    if (phone != null) {
//                        String normalizedPhone = normalizePhoneNumber(phone);
//
//                        if (phoneContactMap.containsKey(normalizedPhone)) {
//                            addContactModel contact = phoneContactMap.get(normalizedPhone);
//
//                            String status = userSnapshot.child("status").getValue(String.class);
//                            if (status != null) {
//                                contact.setStatus(status);
//                            } else {
//                                contact.setStatus("Online");
//                            }
//
//                            registeredContacts.add(contact);
//
//                            phoneContactMap.remove(normalizedPhone);
//                        }
//                    }
//                }
//
//                for (addContactModel contact : phoneContactMap.values()) {
//                    contact.setStatus("Invite to app");
//                    unregisteredContacts.add(contact);
//                }
//
//                if (listener != null) {
//                    listener.onContactSyncComplete(registeredContacts, unregisteredContacts);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e(TAG, "Firebase query cancelled", databaseError.toException());
//                if (listener != null) {
//                    listener.onContactSyncFailed(databaseError.getMessage());
//                }
//            }
//        });
//    }
//
//    private List<addContactModel> getDeviceContacts() {
//        List<addContactModel> contactList = new ArrayList<>();
//        ContentResolver contentResolver = context.getContentResolver();
//
//        Cursor cursor = contentResolver.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                new String[]{
//                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                        ContactsContract.CommonDataKinds.Phone.NUMBER
//                },
//                null,
//                null,
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//        );
//
//        if (cursor != null && cursor.getCount() > 0) {
//            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//            int phoneColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//
//            if (nameColumnIndex >= 0 && phoneColumnIndex >= 0) {
//                while (cursor.moveToNext()) {
//                    String name = cursor.getString(nameColumnIndex);
//                    String phoneNumber = cursor.getString(phoneColumnIndex);
//
//                    if (name != null && phoneNumber != null) {
//                        addContactModel contact = new addContactModel(
//                                R.drawable.user_info,
//                                name,
//                                phoneNumber,
//                                ""
//                        );
//                        contactList.add(contact);
//                    }
//                }
//            } else {
//                Log.e(TAG, "Column not found: DISPLAY_NAME or NUMBER");
//            }
//            cursor.close();
//        }
//
//        return contactList;
//    }
//
//    private String normalizePhoneNumber(String phoneNumber) {
//        return phoneNumber.replaceAll("[^0-9]", "");
//    }
//}
