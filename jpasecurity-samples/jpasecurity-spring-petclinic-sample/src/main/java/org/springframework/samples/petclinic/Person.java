package org.springframework.samples.petclinic;

/**
 * Simple JavaBean domain object representing an person.
 *
 * @author Ken Krebs
 * @author Arne Limburg
 */
public class Person extends BaseEntity {

	private String firstName;
	private String lastName;
    private Credential credential;

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
    
    public Credential getCredential() {
        return credential;
    }
    
    public void setCredential(Credential credential) {
        this.credential = credential;
    }

	public String toString() {
	    return this.getLastName() + ", " + this.getFirstName();
    }
}
