package com.project.chatapp.screen.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.R;
import com.project.chatapp.data.FirebaseMessengerRepository;
import com.project.chatapp.databinding.FragmentContactBinding;
import com.project.chatapp.model.Contact.contact.ContactModel;
import com.project.chatapp.model.Contact.contact.CustomAdapterRVContact;
import com.project.chatapp.screen.chat.ChatsActivity;
import com.project.chatapp.screen.chat.MessageActivity;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {

    private FragmentContactBinding binding;
    private List<ContactModel> contacts;
    private CustomAdapterRVContact adapter;
    private ContactSearch search;

    private ImageButton btnPlus;

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
        adapter = new CustomAdapterRVContact(contacts, contact -> {
            // Khi click vào bạn bè, mở ChatActivity và truyền dữ liệu
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.putExtra("contact", contact);
            startActivity(intent);
        });
        btnPlus = view.findViewById(R.id.btnPlus);
        binding.rwMessenger.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rwMessenger.setAdapter(adapter);
        btnPlus.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddContactActivity.class);
            startActivity(intent);
        });

        loadContactsFromFirebase();
        setupSearchListener();  // thêm tìm kiếm
    }

    //Lấy ds bạn bè
    private void loadContactsFromFirebase() {
        FirebaseMessengerRepository repo = new FirebaseMessengerRepository();
        repo.getFriendListRealtime(friends -> {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (search != null) {
                    List<ContactModel> filtered = search.filter(s.toString());
                    adapter.updateData(filtered);  // cập nhật adapter
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}