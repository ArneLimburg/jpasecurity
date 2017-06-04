/*
 * Copyright 2014 - 2016 Stefan Hildebrandt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Stefan Hildebrandt
 */
public class EntityManagerFirstLevelCacheTest {

    public static final String USER = "user";

    @Rule
    public TestEntityManager entityManager = new TestEntityManager("entity-lifecycle-test");

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);
    }

    @After
    public void unauthenticate() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterFlush() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.flush();
        FieldAccessAnnotationTestBean entityByFind =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
        entityManager.getTransaction().rollback();
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterCommit() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        FieldAccessAnnotationTestBean entityByFind =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
    }

    @Test
    public void removeEntityCreatedWithinSameTransaction() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.flush();
        entityManager.remove(test);
        entityManager.flush();
        FieldAccessAnnotationTestBean shouldNotExists =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
        entityManager.getTransaction().rollback();
    }

    @Test
    public void removeEntityCreatedWithinSameLongRunningEntityManager() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        entityManager.remove(test);
        entityManager.getTransaction().commit();
        FieldAccessAnnotationTestBean shouldNotExists =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerCommitting() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        entityManager.persist(child);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        entityManager.createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        entityManager.getTransaction().commit();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, fieldAccessAnnotationTestBean);
        assertNotNull(fieldAccessAnnotationTestBean);
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        entityManager.getTransaction().commit();
        FieldAccessAnnotationTestBean shouldNotExists =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerFlushing() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.flush();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        entityManager.persist(child);
        entityManager.flush();
        entityManager.createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        entityManager.flush();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotNull(fieldAccessAnnotationTestBean);
        assertSame(test, fieldAccessAnnotationTestBean);
        entityManager.flush();
        entityManager.remove(entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        entityManager.flush();
        FieldAccessAnnotationTestBean shouldNotExists =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        entityManager.remove(fieldAccessAnnotationTestBean2);
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        assertNull(
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        entityManager.getTransaction().commit();
    }

    @Test
    public void removeEntityWithoutFlushingLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        entityManager.remove(fieldAccessAnnotationTestBean2);
        assertNull(
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        entityManager.getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectIsSameWithinOneEntityManager() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            entityManager.getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        entityManager.getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectNotSameAfterRemove() {
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        entityManager.persist(test);
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            entityManager.find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        entityManager.remove(fieldAccessAnnotationTestBean1);
        entityManager.getTransaction().commit();
        FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            entityManager.getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
    }
}
