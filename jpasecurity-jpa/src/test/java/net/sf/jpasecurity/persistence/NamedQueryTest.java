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
package net.sf.jpasecurity.persistence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.jpa.JpaSecurityUnit;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Test;

/**
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class NamedQueryTest {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";

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
        List<FieldAccessAnnotationTestBean> result = entityManager.createNamedQuery("findAllNative").getResultList();
        assertEquals(2, result.size()); //no security, both are accessible
        entityManager.getTransaction().commit();
        entityManager.close();
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
        SecurityUnit securityUnit = new JpaSecurityUnit(persistenceUnitInfo);
        return new OrmXmlParser(securityUnit, new JpaExceptionFactory()).parse();
    }

    @After
    public void logout() {
        TestAuthenticationProvider.authenticate(null);
    }
}
