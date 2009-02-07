package net.sf.jpasecurity.contacts.acl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import net.sf.jpasecurity.acl.AclEntry;

@Entity
public class ContactAclEntry extends AclEntry<Contact, User> {

	protected ContactAclEntry() {
		//for subclassing only
	}
	
	public ContactAclEntry(Contact contact) {
		this(contact, contact.getOwner());
	}
	
    public ContactAclEntry(Contact contact, User user) {
        super(contact, user);
        setRead(true);
    }
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return super.getId();
	}
	
	@ManyToOne
	public Contact getEntity() {
		return super.getEntity();
	}

	@ManyToOne
	public User getOwner() {
		return super.getOwner();
	}
}
