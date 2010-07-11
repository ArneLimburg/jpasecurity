package org.springframework.samples.petclinic;

/**
 * @author Juergen Hoeller
 */
public class PetType extends NamedEntity {

    public boolean equals(Object object) {
        return object instanceof PetType? super.equals(object): false;
    }
}
