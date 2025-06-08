package com.project.chatapp.screen.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.databinding.FragmentContactBinding;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.CustomAdapterRVContact;
import com.project.chatapp.screen.contact.ContactSearch;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private FragmentContactBinding binding;
    private List<ContactModel> contacts;
    private CustomAdapterRVContact adapter;
    private ContactSearch search;


    private final String TAG = "ContactFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contacts = new ArrayList<>();
        adapter = new CustomAdapterRVContact(contacts);
        binding.rwMessenger.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rwMessenger.setAdapter(adapter);

        loadContactsFromFirebase();
        setupSearchListener();  // thêm tìm kiếm
    }

    //Lấy ds bạn bè
    private void loadContactsFromFirebase() {
        FirebaseMessengerRepository repo = new FirebaseMessengerRepository();
        repo.getFriendList(friends -> {
            if (friends != null && !friends.isEmpty()) {
                contacts.clear();
                contacts.addAll(friends);
                search = new ContactSearch(contacts);  // khởi tạo helper
                adapter.updateData(new ArrayList<>(contacts));     // hiển thị ban đầu
            } else {
                Toast.makeText(getContext(), "Không có bạn bè", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSearchListener() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search != null) {
                    List<ContactModel> filtered = search.filter(s.toString());
                    adapter.updateData(filtered);  // cập nhật adapter
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
