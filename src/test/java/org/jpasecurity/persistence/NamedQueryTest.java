/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.persistence.mapping.OrmXmlParser;
import org.jpasecurity.security.authentication.TestAuthenticationProvider;
import org.junit.After;
import org.junit.Test;

/**
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class NamedQueryTest {

    public static final String USER1 = "user1";

    public static final String USER2 = "user2";

    private static final String NON_EXISTING_NAMED_QUERY = "non.existing.query";

    @Test(expected = IllegalArgumentException.class)
    public void createNamedQueryNonExistingNamedQueryThrowsIllegalArgumentException() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNamedQuery(NON_EXISTING_NAMED_QUERY).getResultList();
        entityManager.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNamedQueryWithResultClassNonExistingNamedQueryThrowsIllegalArgumentException() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNamedQuery(NON_EXISTING_NAMED_QUERY, FieldAccessAnnotationTestBean.class).getResultList();
        entityManager.close();
    }

    @Test
    public void createNamedQuery() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        FieldAccessAnnotationTestBean bean2 = setupFieldAccessAnnotationTestData(factory);
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        List<FieldAccessAnnotationTestBean> result = entityManager.createNamedQuery("findAll").getResultList();
        assertEquals(1, result.size()); //the other bean is not accessible
        assertEquals(bean2.getIdentifier(), result.get(0).getIdentifier());
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void createNamedNativeQuery() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        setupFieldAccessAnnotationTestData(factory);
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManager.createNamedQuery("findAllNative").getResultList();
            fail("Should have thrown SecurityException");
        } catch (SecurityException e) {
            // no filtering, so exception is expected
        } finally {
            entityManager.getTransaction().rollback();
            entityManager.close();
        }
    }

    private FieldAccessAnnotationTestBean setupFieldAccessAnnotationTestData(EntityManagerFactory factory) {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean1 = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(bean1);
        TestAuthenticationProvider.authenticate(USER2);
        FieldAccessAnnotationTestBean bean2 = new FieldAccessAnnotationTestBean(USER2);
        entityManager.persist(bean2);
        entityManager.getTransaction().commit();
        entityManager.close();
        return bean2;
    }

    @Test
    public void parseNamedQueryInOrmXml() throws Exception {
        MappingInformation mappingInformation = parseOrmFile("META-INF/named-query.xml");

        assertEquals(1, mappingInformation.getNamedQueryNames().size());
        assertEquals("select test from Contact test", mappingInformation.getNamedQuery("myQuery1"));
    }

    @Test
    public void parseNamedNativeQueryInOrmXml() throws Exception {
        MappingInformation mappingInformation = parseOrmFile("META-INF/named-query.xml");

        assertEquals(1, mappingInformation.getNamedNativeQueryNames().size());
        assertEquals("select test.id from Contact test", mappingInformation.getNamedNativeQuery("myQuery1Native"));
    }

    @Test
    public void parseNamedQueriesInOrmXml() throws Exception {
        MappingInformation mappingInformation = parseOrmFile("META-INF/named-queries.xml");

        assertEquals(4, mappingInformation.getNamedQueryNames().size());

        assertEquals("select test from Contact test", mappingInformation.getNamedQuery("myQuery1"));
        assertEquals("select test from Contact test", mappingInformation.getNamedQuery("myQuery2"));
        assertEquals("select test from Contact test", mappingInformation.getNamedQuery("myQuery3"));
        assertEquals("select test from Contact test", mappingInformation.getNamedQuery("myQuery4"));
    }

    @Test
    public void parseNamedNativeQueriesInOrmXml() throws Exception {
        MappingInformation mappingInformation = parseOrmFile("META-INF/named-queries.xml");

        assertEquals(4, mappingInformation.getNamedQueryNames().size());

        assertEquals("select test.id from Contact test", mappingInformation.getNamedNativeQuery("myQuery1Native"));
        assertEquals("select test.id from Contact test", mappingInformation.getNamedNativeQuery("myQuery2Native"));
        assertEquals("select test.id from Contact test", mappingInformation.getNamedNativeQuery("myQuery3Native"));
        assertEquals("select test.id from Contact test", mappingInformation.getNamedNativeQuery("myQuery4Native"));
    }

    private MappingInformation parseOrmFile(String ormFileName) {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getMappingFileNames().add(ormFileName);
        return new OrmXmlParser(persistenceUnitInfo, new JpaExceptionFactory()).parse();
    }

    @After
    public void logout() {
        TestAuthenticationProvider.authenticate(null);
    }
}
