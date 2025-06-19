package com.project.chatapp.screen.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.project.chatapp.R;
import com.project.chatapp.adapter.OptionAdapter;
import com.project.chatapp.databinding.FragmentMoreBinding;
import com.project.chatapp.model.OptionModel;
import com.project.chatapp.screen.authentication.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {
    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy userId hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        Log.d("MORE_FRAGMENT", "Current userId: " + userId);
        final android.widget.TextView tvUsername = binding.getRoot().findViewById(R.id.textViewUsername);
        final android.widget.TextView tvPhone = binding.getRoot().findViewById(R.id.textViewPhone);
        if (userId != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("MORE_FRAGMENT", "User snapshot: " + snapshot);
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    Log.d("MORE_FRAGMENT", "name=" + name + ", phone=" + phone);
                    if (name != null) tvUsername.setText(name);
                    if (phone != null) tvPhone.setText(phone);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MORE_FRAGMENT", "Firebase error: " + error.getMessage());
                }
            });
        }

        // Cài đặt RecyclerView
        List<OptionModel> optionList = getOptionList();
        OptionAdapter adapter = new OptionAdapter(optionList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(position -> {
            String optionName = getOptionList().get(position).getName();

            switch (optionName) {
                case "Logout":
                    handleLogout();
                    break;
                case "My Profile":
                    break;
                case "Settings":
                    break;
                case "Help & Support":
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private List<OptionModel> getOptionList() {
        List<OptionModel> list = new ArrayList<>();
        list.add(new OptionModel(R.drawable.account, "My Profile"));
        list.add(new OptionModel(R.drawable.chat_nav, "Settings"));
        list.add(new OptionModel(R.drawable.appearance, "Help & Support"));
        list.add(new OptionModel(R.drawable.notification, "Logout"));
        return list;
    }
}
