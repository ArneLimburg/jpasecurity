package org.springframework.samples.petclinic;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 * 
 * @author Juergen Hoeller
 */
public class Specialty extends NamedEntity {
    
    public boolean equals(Object object) {
        return object instanceof Specialty? super.equals(object): false;
    }
}
