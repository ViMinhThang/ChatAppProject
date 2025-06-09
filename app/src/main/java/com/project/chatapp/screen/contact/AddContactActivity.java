package com.project.chatapp.screen.contact;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.model.Contact.addContact.CustomAdapterRVAddContact;
import com.project.chatapp.model.Contact.addContact.addContactModel;

import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {
    FirebaseMessengerRepository repository;
    private RecyclerView rwMessenger;
    private CustomAdapterRVAddContact adapter;
    private AddContactSearch search;
    private List<addContactModel> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        rwMessenger = findViewById(R.id.rvAddContact);
        rwMessenger.setLayoutManager(new LinearLayoutManager(this));

        repository = new FirebaseMessengerRepository();

        adapter = new CustomAdapterRVAddContact(contactList, phone -> {
            repository.addFriendByPhoneNumber(phone);
        });
        rwMessenger.setAdapter(adapter);

        loadContacts();
        setupSearchListener();
    }

    private void loadContacts() {
        repository.getAllUsersExceptCurrent(users -> {
            contactList.clear();
            contactList.addAll(users);
            if (search == null) {
                search = new AddContactSearch(users);
            }
            adapter.updateData(contactList);
        });
    }

    private void setupSearchListener() {
        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search != null) {
                    List<addContactModel> filtered = search.filter(s.toString());
                    adapter.updateData(filtered);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
