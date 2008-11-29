package org.springframework.samples.petclinic.jpa;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.PetType;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA implementation of the Clinic interface using EntityManager.
 *
 * <p>The mappings are defined in "orm.xml" located in the META-INF directory.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Arne Limburg
 * @since 22.4.2006
 */
@Repository
@Transactional
public class EntityManagerClinic implements Clinic {

	@PersistenceContext
	private EntityManager em;


	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<Vet> getVets() {
		Query query = this.em.createQuery("SELECT DISTINCT vet FROM Vet vet "
                                        + "LEFT OUTER JOIN FETCH vet.specialtiesInternal "
                                        + "LEFT OUTER JOIN FETCH vet.visitsInternal "
                                        + "ORDER BY vet.lastName, vet.firstName");
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<PetType> getPetTypes() {
        Query query = this.em.createQuery("SELECT ptype FROM PetType ptype "
                                        + "ORDER BY ptype.name");
        return query.getResultList();
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public Collection<Owner> findOwners(String lastName) {
		Query query = this.em.createQuery("SELECT owner FROM Owner owner "
                                        + "LEFT OUTER JOIN FETCH owner.petsInternal pets "
                                        + "WHERE owner.lastName LIKE :lastName");
		query.setParameter("lastName", lastName + "%");
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	public Owner loadOwner(int id) {
		Query query = this.em.createQuery("SELECT owner FROM Owner owner "
                                        + "LEFT OUTER JOIN FETCH owner.petsInternal pets "
                                        + "LEFT OUTER JOIN FETCH pets.visitsInternal visits "
                                        + "LEFT OUTER JOIN FETCH visits.vet vet "
                                        + "WHERE owner.id = :id");
        query.setParameter("id", id);
        return (Owner)query.getSingleResult();
	}

	@Transactional(readOnly = true)
	public Pet loadPet(int id) {
        Query query = this.em.createQuery("SELECT pet FROM Pet pet "
                                        + "LEFT OUTER JOIN FETCH pet.owner owner "
                                        + "LEFT OUTER JOIN FETCH pet.visitsInternal visits "
                                        + "WHERE pet.id = :id");
        query.setParameter("id", id);
        return (Pet)query.getSingleResult();
	}

    @Transactional(readOnly = true)
    public Vet loadVet(int id) {
        Query query = this.em.createQuery("SELECT vet FROM Vet vet "
                                        + "LEFT OUTER JOIN FETCH vet.specialtiesInternal specialities "
                                        + "LEFT OUTER JOIN FETCH vet.visitsInternal visit "
                                        + "LEFT OUTER JOIN FETCH visit.pet pet "
                                        + "LEFT OUTER JOIN FETCH pet.owner owner "
                                        + "LEFT OUTER JOIN FETCH pet.type petType "
                                        + "WHERE vet.id = :id");
        query.setParameter("id", id);
        return (Vet)query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public Visit loadVisit(int id) {
        Query query = this.em.createQuery("SELECT visit FROM Visit visit "
                                        + "LEFT OUTER JOIN FETCH visit.vet vet "
                                        + "LEFT OUTER JOIN FETCH vet.specialtiesInternal specialties "
                                        + "LEFT OUTER JOIN FETCH visit.pet pet "
                                        + "LEFT OUTER JOIN FETCH pet.owner owner "
                                        + "LEFT OUTER JOIN FETCH pet.type petType "
                                        + "LEFT OUTER JOIN FETCH pet.visitsInternal visits "
                                        + "WHERE visit.id = :id");
        query.setParameter("id", id);
        return (Visit)query.getSingleResult();
    }

	public void storeOwner(Owner owner) {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		Owner merged = this.em.merge(owner);
		this.em.flush();
		owner.setId(merged.getId());
	}

	public void storePet(Pet pet) {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		Pet merged = this.em.merge(pet);
		this.em.flush();
		pet.setId(merged.getId());
	}

	public void storeVisit(Visit visit) {
		// Consider returning the persistent object here, for exposing
		// a newly assigned id using any persistence provider...
		Visit merged = this.em.merge(visit);
		this.em.flush();
		visit.setId(merged.getId());
	}

}
