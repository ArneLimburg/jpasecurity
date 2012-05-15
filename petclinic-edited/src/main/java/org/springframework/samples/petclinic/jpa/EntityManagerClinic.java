/*
 * Copyright 2008 Mike Keith, Rod Johnson, Sam Brannen, Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
        return this.em.createQuery("SELECT vet FROM Vet vet ORDER BY vet.lastName, vet.firstName").getResultList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Collection<PetType> getPetTypes() {
        return this.em.createQuery("SELECT ptype FROM PetType ptype ORDER BY ptype.name").getResultList();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Collection<Owner> findOwners(String lastName) {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Collection<Visit> findVisits(Vet vet) {
        Query query = this.em.createQuery("SELECT visit FROM Visit visit "
                + "WHERE visit.vet = :vet");
        query.setParameter("vet", vet);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Owner loadOwner(int id) {
        return this.em.find(Owner.class, id);
    }

    @Transactional(readOnly = true)
    public Pet loadPet(int id) {
        return this.em.find(Pet.class, id);
    }

    @Transactional(readOnly = true)
    public Vet loadVet(int id) {
        return this.em.find(Vet.class, id);
    }

    @Transactional(readOnly = true)
    public Visit loadVisit(int id) {
        return this.em.find(Visit.class, id);
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

    public void deletePet(int id) {
        Pet pet = loadPet(id);
        this.em.remove(pet);
    }
}
