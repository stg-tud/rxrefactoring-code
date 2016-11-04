package careercup.contactbook;

import java.util.Set;
import java.util.TreeSet;

public class ContactBook {

    Set<Contact> contacts;

    public ContactBook() {
        contacts = new TreeSet<Contact>();
    }

    public boolean addContact(Contact c) {
        return contacts.add(c);
    }

    public boolean removeContact(Contact c) {
        return contacts.remove(c);
    }

    Set<Contact> getAllContacts() {
        return contacts;
    }
}
