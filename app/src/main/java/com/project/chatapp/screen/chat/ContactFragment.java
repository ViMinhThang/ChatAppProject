package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.CustomAdapterRVContact;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ImageButton btnPlus;
    private List<ContactModel> contacts;
    private List<ContactModel> filteredContacts;
    private CustomAdapterRVContact adapter;
    private static final int ADD_CONTACT_REQUEST = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        recyclerView = view.findViewById(R.id.rwMessenger);
        searchEditText = view.findViewById(R.id.searchEditText);
        btnPlus = view.findViewById(R.id.btnPlus);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contacts = new ArrayList<>();
        filteredContacts = new ArrayList<>();

        adapter = new CustomAdapterRVContact(filteredContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddContactActivity.class);
            startActivityForResult(intent, ADD_CONTACT_REQUEST);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterContacts(s.toString());
            }
        });

        loadSampleContacts();
    }

    private void filterContacts(String query) {
        filteredContacts.clear();

        if (query.isEmpty()) {
            filteredContacts.addAll(contacts);
        } else {
            query = query.toLowerCase();
            for (ContactModel contact : contacts) {
                if (contact.getName().toLowerCase().contains(query) ||
                        contact.getPhone().contains(query)) {
                    filteredContacts.add(contact);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void loadSampleContacts() {

        filteredContacts.addAll(contacts);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            ContactModel newContact = (ContactModel) data.getSerializableExtra("new_contact");
            if (newContact != null) {
                contacts.add(newContact);
                filteredContacts.add(newContact);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
