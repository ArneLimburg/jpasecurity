package org.springframework.samples.petclinic;

/**
 * Simple JavaBean domain object with an id property.
 * Used as a base class for objects needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class BaseEntity {

	private Integer id;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public boolean isNew() {
		return (this.id == null);
	}

    public int hashCode() {
        if (isNew()) {
            return System.identityHashCode(this);
        } else {
            return id;
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof BaseEntity)) {
            return false;
        }
        if (isNew()) {
            return this == object;
        }
        return getId().equals(((BaseEntity)object).getId());
    }
}
