/*
 * Copyright 2010 - 2016 Arne Limburg
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
package org.jpasecurity.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.MethodAccessAnnotationTestBean;
import org.jpasecurity.persistence.ParentChildTestData;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessCheckTest {

    private static final String CREATOR = "creator";
    private static final String USER = "user";
    private static final String USER1 = "user1";
    private static final String USER2 = "user2";
    private static final String ADMIN = "admin";
    private static final String CHILD = "child";
    private static final String GRANDCHILD = "grandchild";

    @Test
    public void create() {
        TestSecurityContext.authenticate(CREATOR, CREATOR);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("access-check");

        //Merge a new entity
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(CHILD);
        FieldAccessAnnotationTestBean grandchild = new FieldAccessAnnotationTestBean(GRANDCHILD);
        child.setParentBean(bean);
        bean.getChildBeans().add(child);
        grandchild.setParentBean(child);
        child.getChildBeans().add(grandchild);
        bean = entityManager.merge(bean);
        entityManager.getTransaction().commit();
        entityManager.close();

        //Merge an existing entity
        try {
            entityManager = factory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.merge(bean);
            entityManager.getTransaction().commit();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    @Test
    public void update() {
        TestSecurityContext.authenticate(ADMIN, ADMIN);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("access-check");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        entityManager.getTransaction().commit();
        entityManager.close();

        TestSecurityContext.authenticate(USER);
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        try {
            bean.setBeanName("new BeanName");
            entityManager.getTransaction().commit();
            fail("expected security exception");
        } catch (PersistenceException e) {
            assertEquals(SecurityException.class, e.getCause().getClass());
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        }
        entityManager.close();
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void hibernateWith() {
        TestSecurityContext.authenticate(ADMIN, ADMIN);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("with-clause");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        ParentChildTestData testData = new ParentChildTestData(entityManager);
        testData.createPermutations(USER1, USER2);
        entityManager.getTransaction().commit();
        entityManager.close();

        TestSecurityContext.authenticate(USER1);
        entityManager = factory.createEntityManager();
        Query query = entityManager.createQuery("SELECT mbean FROM MethodAccessAnnotationTestBean mbean "
                                                + "WHERE mbean.name = :name");
        query.setParameter("name", USER1);
        List<MethodAccessAnnotationTestBean> result = query.getResultList();
        assertEquals(1, result.size());

        TestSecurityContext.authenticate(ADMIN, ADMIN);
        MethodAccessAnnotationTestBean bean = result.iterator().next();
        assertEquals(USER1, bean.getName());
        assertEquals(USER2, ((MethodAccessAnnotationTestBean)bean.getParent()).getName());

        try {
            TestSecurityContext.authenticate(USER1);
            entityManager.getTransaction().begin();
            ((MethodAccessAnnotationTestBean)bean.getParent()).setName(USER1);
            entityManager.getTransaction().commit();
            fail("expected SecurityException");
        } catch (PersistenceException e) {
            assertEquals(SecurityException.class, e.getCause().getClass());
        }
        entityManager.close();
    }

    @Test
    @Ignore("TODO replace persistence provider")
    public void aliasRules() {
        TestSecurityContext.authenticate(USER);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("alias");

        EntityManager entityManager = factory.createEntityManager();
        EntityManager mock = (EntityManager)entityManager.getDelegate();
        reset(mock);
        String originalQuery = "SELECT c.text, SUM(c.id) AS cSum FROM Contact AS c "
                             + "WHERE c.owner.name = :name GROUP BY c.text ORDER BY cSum";
        // TODO check why the rule is applied twice
        String filteredQuery = " SELECT c.text,  SUM(c.id)  AS cSum FROM Contact c "
                             + "WHERE (c.owner.name = :name) AND (c.owner.name = 'user') "
                             + "GROUP BY c.text  ORDER BY cSum";

        when(mock.isOpen()).thenReturn(true);
        when(mock.getFlushMode()).thenReturn(FlushModeType.AUTO);
        when(mock.createQuery(filteredQuery)).thenReturn(mock(Query.class));

        entityManager.createQuery(originalQuery);

        verify(mock).createQuery(filteredQuery);
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }
}
