package com.project.chatapp.screen.contact;

import com.project.chatapp.model.Contact.contact.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class ContactSearch {
    private final List<ContactModel> originalList;

    public ContactSearch(List<ContactModel> originalList) {
        this.originalList = new ArrayList<>(originalList); // sao chép để giữ nguyên dữ liệu gốc
    }

    public List<ContactModel> filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(originalList); // trả lại toàn bộ danh sách
        }

        List<ContactModel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (ContactModel contact : originalList) {
            if (contact.getName() != null && contact.getName().toLowerCase().contains(lowerQuery)) {
                filteredList.add(contact);
            }
        }

        return filteredList;
    }
}
