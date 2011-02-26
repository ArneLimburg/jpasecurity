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
package net.sf.jpasecurity.sample.simple;

import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.security.authentication.StaticAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class App {

    public static void main(String[] args) {
        StaticAuthenticationProvider.authenticate("John");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("contacts");
        createUsers(entityManagerFactory);
        displayUserCount(entityManagerFactory);
        createContacts(entityManagerFactory);
        displayContactCount(entityManagerFactory);
    }

    public static void createUsers(final EntityManagerFactory entityManagerFactory) {
        StaticAuthenticationProvider.runAs("root", Arrays.asList("admin"), new PrivilegedAction<Object>() {
            public Object run() {
                EntityManager entityManager;

                entityManager = entityManagerFactory.createEntityManager();
                entityManager.getTransaction().begin();
                entityManager.persist(new User("John"));
                entityManager.persist(new User("Mary"));
                entityManager.getTransaction().commit();
                entityManager.close();
                return null;
            }
        });
    }

    public static void displayUserCount(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<User> users = entityManager.createQuery("SELECT user FROM User user").getResultList();
        System.out.println("users.size = " + users.size());
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public static void createContacts(final EntityManagerFactory entityManagerFactory) {
        StaticAuthenticationProvider.runAs("root", Arrays.asList("admin"), new PrivilegedAction<Object>() {
            public Object run() {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                entityManager.getTransaction().begin();
                Query johnQuery = entityManager.createQuery("SELECT user FROM User user WHERE user.name = 'John'");
                Query maryQuery = entityManager.createQuery("SELECT user FROM User user WHERE user.name = 'Mary'");
                User john = (User)johnQuery.getSingleResult();
                User mary = (User)maryQuery.getSingleResult();
                entityManager.persist(new Contact(john, "peter@jpasecurity.sf.net"));
                entityManager.persist(new Contact(john, "0 12 34 - 56 789"));
                entityManager.persist(new Contact(mary, "paul@jpasecurity.sf.net"));
                entityManager.persist(new Contact(mary, "12 34 56 78 90"));
                entityManager.getTransaction().commit();
                entityManager.close();
                return null;
            }
        });
    }

    public static void displayContactCount(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager;

        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Contact> contacts = entityManager.createQuery("SELECT contact FROM Contact contact").getResultList();
        System.out.println("contacts.size = " + contacts.size());
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
