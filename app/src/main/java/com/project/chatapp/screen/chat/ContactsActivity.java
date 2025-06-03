package com.project.chatapp.screen.chat;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.project.chatapp.R;
import com.project.chatapp.databinding.ActivityContactsBinding;
import com.project.chatapp.model.Contact.ContactModel;
import com.project.chatapp.model.Contact.CustomAdapterRVContact;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {
    private ActivityContactsBinding binding ;
    private List<ContactModel> contacts ;
    private CustomAdapterRVContact adapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityContactsBinding.inflate(getLayoutInflater()) ;
        setContentView(binding.getRoot());



        contacts = new ArrayList<>() ;



        adapter = new CustomAdapterRVContact(contacts);
        binding.rwMessenger.setLayoutManager(new LinearLayoutManager(this));
        binding.rwMessenger.setAdapter(adapter);
    }
}