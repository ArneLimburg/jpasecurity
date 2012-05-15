/*
 * Copyright 2008 Juergen Hoeller, Ken Krebs, Sam Brannen, Arne Limburg
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
package org.springframework.samples.petclinic;

import java.util.Collection;


/**
 * The high-level PetClinic business interface.
 *
 * <p>This is basically a data access object.
 * PetClinic doesn't have a dedicated business facade.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arne Limburg
 */
public interface Clinic {

    /**
     * Retrieve all <code>Vet</code>s from the data store.
     * @return a <code>Collection</code> of <code>Vet</code>s
     */
    Collection<Vet> getVets();

    /**
     * Retrieve all <code>PetType</code>s from the data store.
     * @return a <code>Collection</code> of <code>PetType</code>s
     */
    Collection<PetType> getPetTypes();

    /**
     * Retrieve <code>Owner</code>s from the data store by last name,
     * returning all owners whose last name <i>starts</i> with the given name.
     * @param lastName Value to search for
     * @return a <code>Collection</code> of matching <code>Owner</code>s
     * (or an empty <code>Collection</code> if none found)
     */
    Collection<Owner> findOwners(String lastName);

    /**
     * Retrieve <code>Visit</code>s from the data store,
     * returning all visits at a given vet.
     * @param vet the visited vet
     * @return a <code>Collection</code> of matching <code>Visit</code>s
     * (or an empty <code>Collection</code> if none found)
     */
    Collection<Visit> findVisits(Vet vet);

    /**
     * Retrieve an <code>Owner</code> from the data store by id.
     * @param id the id to search for
     * @return the <code>Owner</code> if found
     */
    Owner loadOwner(int id);

    /**
     * Retrieve a <code>Pet</code> from the data store by id.
     * @param id the id to search for
     * @return the <code>Pet</code> if found
     */
    Pet loadPet(int id);

    /**
     * Retrieve a <code>Vet</code> from the data store by id.
     * @param id the id to search for
     * @return the <code>Vet</code> if found
     */
    Vet loadVet(int id);

    /**
     * Retrieve a <code>Visit</code> from the data store by id.
     * @param id the id to search for
     * @return the <code>Visit</code> if found
     */
    Visit loadVisit(int id);

    /**
     * Save an <code>Owner</code> to the data store, either inserting or updating it.
     * @param owner the <code>Owner</code> to save
     * @see BaseEntity#isNew
     */
    void storeOwner(Owner owner);

    /**
     * Save a <code>Pet</code> to the data store, either inserting or updating it.
     * @param pet the <code>Pet</code> to save
     * @see BaseEntity#isNew
     */
    void storePet(Pet pet);

    /**
     * Save a <code>Visit</code> to the data store, either inserting or updating it.
     * @param visit the <code>Visit</code> to save
     * @see BaseEntity#isNew
     */
    void storeVisit(Visit visit);

    /**
     * Deletes a <code>Pet</code> from the data store.
     */
    void deletePet(int id);
}
