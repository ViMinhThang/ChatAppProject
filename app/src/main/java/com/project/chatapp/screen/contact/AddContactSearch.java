package com.project.chatapp.screen.contact;

import com.project.chatapp.model.Contact.addContact.addContactModel;
import com.project.chatapp.model.Contact.contact.ContactModel;

import java.util.ArrayList;
import java.util.List;

public class AddContactSearch {
    private final List<addContactModel> originalList;

    public AddContactSearch(List<addContactModel> originalList) {
        this.originalList = new ArrayList<>(originalList);
    }

    public List<addContactModel> filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(originalList);
        }

        List<addContactModel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (addContactModel contact : originalList) {
            if (contact.getPhone() != null && contact.getPhone().toLowerCase().contains(lowerQuery)) {
                filteredList.add(contact);
            }
        }

        return filteredList;
    }
}
