package com.project.chatapp.screen.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.databinding.FragmentContactBinding;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.CustomAdapterRVContact;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private FragmentContactBinding binding;
    private List<ContactModel> contacts;
    private CustomAdapterRVContact adapter;

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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
