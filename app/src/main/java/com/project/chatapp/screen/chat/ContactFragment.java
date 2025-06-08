package com.project.chatapp.screen.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.CustomAdapterRVContact;
import com.project.chatapp.utils.ContactSyncManager;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment implements ContactSyncManager.OnContactSyncListener {
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ImageButton btnPlus;
    private List<ContactModel> allContacts;
    private List<ContactModel> filteredContacts;
    private CustomAdapterRVContact adapter;
    private static final int ADD_CONTACT_REQUEST = 100;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 101;
    private ContactSyncManager contactSyncManager;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        contactSyncManager = new ContactSyncManager(requireContext());
        contactSyncManager.setOnContactSyncListener(this);

        recyclerView = view.findViewById(R.id.rwMessenger);
        searchEditText = view.findViewById(R.id.searchEditText);
        btnPlus = view.findViewById(R.id.btnPlus);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allContacts = new ArrayList<>();
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

        checkPermissionAndSyncContacts();
    }

    private void checkPermissionAndSyncContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            syncContacts();
        }
    }

    private void syncContacts() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        contactSyncManager.syncContacts();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, tiến hành đồng bộ
                syncContacts();
            } else {
                // Quyền bị từ chối
                Toast.makeText(getContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
                emptyView.setText("Permission denied to read contacts");
                emptyView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onContactSyncComplete(List<ContactModel> registeredContacts, List<ContactModel> unregisteredContacts) {
        progressBar.setVisibility(View.GONE);

        allContacts.clear();

        if (!registeredContacts.isEmpty()) {
            ContactModel suggestHeader = new ContactModel(0, "Gợi ý kết bạn", "", "header");
            allContacts.add(suggestHeader);
            allContacts.addAll(registeredContacts);
        }

        if (!unregisteredContacts.isEmpty()) {
            ContactModel inviteHeader = new ContactModel(0, "Mời bạn bè dùng app", "", "header");
            allContacts.add(inviteHeader);
            allContacts.addAll(unregisteredContacts);
        }

        filteredContacts.clear();
        filteredContacts.addAll(allContacts);

        if (allContacts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setText("No contacts found");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onContactSyncFailed(String error) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setText("Error: " + error);
        emptyView.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Sync failed: " + error, Toast.LENGTH_SHORT).show();
    }

    private void filterContacts(String query) {
        filteredContacts.clear();

        if (query.isEmpty()) {
            filteredContacts.addAll(allContacts);
        } else {
            query = query.toLowerCase();
            for (ContactModel contact : allContacts) {
                if ("header".equals(contact.getStatus())) {
                    continue;
                }

                if (contact.getName().toLowerCase().contains(query) ||
                        contact.getPhone().contains(query)) {
                    filteredContacts.add(contact);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CONTACT_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
        }
    }
}
