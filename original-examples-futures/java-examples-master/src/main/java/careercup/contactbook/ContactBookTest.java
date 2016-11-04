package careercup.contactbook;

import java.util.Iterator;

import org.junit.Test;

public class ContactBookTest {

    @Test
    public void contactBookTest() {
        ContactBook book = new ContactBook();
        book.addContact(new Contact("shashank", "mani", "8804567889"));
        book.addContact(new Contact("rahul", "sharma", "90045067889"));
        book.addContact(new Contact("mohan", "mani", "9894568089"));
        book.addContact(new Contact("rajesh", "sharma", "12885607889"));
        book.addContact(new Contact("ashutosh", "mani", "7834567889"));
        book.addContact(new Contact("raja", "goyal", "6734567889"));
        book.addContact(new Contact("mayank", "sharma", "2234467889"));
        book.addContact(new Contact("bullu", "verma", "1234568809"));
        book.addContact(new Contact("nimish", "narayan", "8256907834"));
        book.addContact(new Contact("c.p", "sharm", "9256738890"));

        Iterator<Contact> it = book.getAllContacts().iterator();
        while (it.hasNext())
            System.out.println(it.next());
    }

}
