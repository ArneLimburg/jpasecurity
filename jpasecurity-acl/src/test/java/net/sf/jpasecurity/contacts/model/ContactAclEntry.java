package net.sf.jpasecurity.contacts.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

import net.sf.jpasecurity.acl.AclEntry;
import net.sf.jpasecurity.acl.AclEntryIdentifier;

@Entity
//@IdClass(AclEntryIdentifier.class)
public class ContactAclEntry extends AclEntry<Contact, User> {

	private int id;
	
	protected ContactAclEntry() {
		//for subclassing only
	}
	
	public ContactAclEntry(Contact contact) {
		super(contact, contact.getOwner());
		setRead(true);
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
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
