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
package org.jpasecurity.contacts;

import javax.annotation.security.DeclareRoles;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;

/**
 * @author Arne Limburg
 */
@Singleton
@DependsOn("ContactsDatabase")
@DeclareRoles("admin")
public class ContactsTestData {

    @PersistenceContext(name = "ejb-contacts", unitName = "ejb-contacts")
    private EntityManager entityManager;
    protected User john;
    protected User mary;
    protected Contact johnsContact1;
    protected Contact johnsContact2;
    protected Contact marysContact1;
    protected Contact marysContact2;

    public void createTestData() {
        createTestData(entityManager);
    }

    public void createTestData(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        createTestData(entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    protected void createTestData(EntityManager entityManager) {
        john = createUser(entityManager, "John");
        mary = createUser(entityManager, "Mary");
        johnsContact1 = createContact(entityManager, john, "john@jpasecurity.sf.net");
        johnsContact2 = createContact(entityManager, john, "0 12 34 - 56 789");
        marysContact1 = createContact(entityManager, mary, "mary@jpasecurity.sf.net");
        marysContact2 = createContact(entityManager, mary, "12 34 56 78 90");
    }

    public User getJohn() {
        return john;
    }

    public User getMary() {
        return mary;
    }

    public Contact getJohnsContact1() {
        return johnsContact1;
    }

    public Contact getJohnsContact2() {
        return johnsContact2;
    }

    public Contact getMarysContact1() {
        return marysContact1;
    }

    public Contact getMarysContact2() {
        return marysContact2;
    }

    public void clearTestData(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        clearTestData(entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    protected User createUser(EntityManager entityManager, String name) {
        User user = new User(name);
        entityManager.persist(user);
        return user;
    }

    protected Contact createContact(EntityManager entityManager, User owner, String text) {
        Contact contact = new Contact(owner, text);
        entityManager.persist(contact);
        return contact;
    }

    protected void clearTestData(EntityManager entityManager) {
        entityManager.createQuery("delete from Contact contact").executeUpdate();
        entityManager.createQuery("delete from User user").executeUpdate();
    }
}
