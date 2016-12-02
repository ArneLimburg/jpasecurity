/*
 * Copyright 2008 Arne Limburg
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
package org.jpasecurity.spring.contacts;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;

/**
 * @author Arne Limburg
 */
public class DefaultContactsDao implements ContactsDao {

    @PersistenceContext
    private EntityManager entityManager;

    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT user FROM User user").getResultList();
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    public User getUser(String name) {
        return (User)entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name")
                                  .setParameter("name", name)
                                  .getSingleResult();
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Contact> getAllContacts() {
        String query = "SELECT contact FROM Contact contact INNER JOIN FETCH contact.owner user";
        return entityManager.createQuery(query).getResultList();
    }
}
