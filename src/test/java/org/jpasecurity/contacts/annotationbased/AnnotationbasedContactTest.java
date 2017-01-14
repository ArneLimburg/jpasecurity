/*
 * Copyright 2017 Arne Limburg
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
package org.jpasecurity.contacts.annotationbased;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnnotationbasedContactTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("annotationbased-contacts",
                Collections.singletonMap("hibernate.show_sql", "true"));
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    @Before
    public void insertTestData() {
        entityManager = entityManagerFactory.createEntityManager();
        TestSecurityContext.authenticate("admin", "admin");
        entityManager.getTransaction().begin();
        entityManager.persist(new Contact("mary", "Marys contact"));
        entityManager.persist(new Contact("john", "Johns contact"));
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @After
    public void deleteTestData() {
        TestSecurityContext.authenticate("admin", "admin");
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM Contact contact").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
        TestSecurityContext.unauthenticate();
    }

    @Test
    public void findAllIsFiltered() {
        TestSecurityContext.authenticate("admin", "admin");
        List<Contact> result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));

        TestSecurityContext.authenticate("john", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next().getOwner(), is("john"));

        TestSecurityContext.authenticate("mary", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next().getOwner(), is("mary"));
    }
}
