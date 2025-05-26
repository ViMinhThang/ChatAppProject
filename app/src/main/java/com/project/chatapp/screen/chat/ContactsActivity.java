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
        contacts.add(new ContactModel(R.drawable.pic1,"Vợ yêu 1" , "Last seen yesterday")) ;
        contacts.add(new ContactModel(R.drawable.pic2,"Vợ yêu 2" , "Online")) ;
        contacts.add(new ContactModel(R.drawable.pic3,"Vợ yêu 3" , "Last seen 3 hour ago")) ;
        contacts.add(new ContactModel(R.drawable.pic4,"Vợ yêu 4" , "Online")) ;
        contacts.add(new ContactModel(R.drawable.pic5,"Vợ yêu 5" , "Online")) ;
        contacts.add(new ContactModel(R.drawable.pic6,"Vợ yêu 6" , "Last seen 30 minutes")) ;

        adapter = new CustomAdapterRVContact(contacts);
        binding.rwMessenger.setLayoutManager(new LinearLayoutManager(this));
        binding.rwMessenger.setAdapter(adapter);
    }
}