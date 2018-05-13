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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class AnnotationbasedContactTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private Contact marysContact;
    private Contact johnsContact;
    private Contact publicContact;
    private Contact rootContact;

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
        marysContact = new Contact("mary", "Marys contact");
        johnsContact = new Contact("john", "Johns contact");
        publicContact = new Contact("public", "public");
        rootContact = new Contact("root", "root contact");
        entityManager.persist(marysContact);
        entityManager.persist(johnsContact);
        entityManager.persist(publicContact);
        entityManager.persist(rootContact);
        entityManager.getTransaction().commit();
        entityManager.clear();
        TestSecurityContext.unauthenticate();
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
    @Ignore("Ignored until grammar is fixed")
    public void findAllIsFiltered() {
        List<Contact> result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(publicContact));

        TestSecurityContext.authenticate("admin", "admin");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems(marysContact, johnsContact, publicContact, rootContact));

        TestSecurityContext.authenticate("john", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(johnsContact, publicContact));

        TestSecurityContext.authenticate("mary", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(marysContact, publicContact));
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void findAllWithCriteria() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Contact> query = cb.createQuery(Contact.class);
        query.from(Contact.class);
        List<Contact> result = entityManager.createQuery(query).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result, hasItem(publicContact));

        TestSecurityContext.authenticate("admin", "admin");
        query = cb.createQuery(Contact.class);
        query.from(Contact.class);
        result = entityManager.createQuery(query).getResultList();
        assertThat(result.size(), is(4));
        assertThat(result, hasItems(marysContact, johnsContact, publicContact, rootContact));

        TestSecurityContext.authenticate("john", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(johnsContact, publicContact));

        TestSecurityContext.authenticate("mary", "user");
        result = entityManager.createNamedQuery(Contact.FIND_ALL, Contact.class).getResultList();
        assertThat(result.size(), is(2));
        assertThat(result, hasItems(marysContact, publicContact));
    }
}
