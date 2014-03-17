/*
 * Copyright 2014 Stefan Hildebrandt
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
package net.sf.jpasecurity.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Stefan Hildebrandt
 */
public class EntityManagerFirstLevelCacheTest extends AbstractEntityTestCase {

    public static final String USER = "user";

    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("entity-lifecycle-test");
    }

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(USER);
    }

    @After
    public void unauthenticate() {
        TestAuthenticationProvider.authenticate(null);
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterFlush() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().flush();
        final FieldAccessAnnotationTestBean entityByFind =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
        getEntityManager().getTransaction().rollback();
    }

    @Test
    public void findUnProxiedEntityWithinSameEntityManagerCreatedAfterCommit() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        final FieldAccessAnnotationTestBean entityByFind =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, entityByFind);
    }

    @Test
    public void removeEntityCreatedWithinSameTransaction() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().flush();
        getEntityManager().remove(test);
        getEntityManager().flush();
        final FieldAccessAnnotationTestBean shouldNotExists =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
        getEntityManager().getTransaction().rollback();
    }

    @Test
    public void removeEntityCreatedWithinSameLongRunningEntityManager() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        getEntityManager().getTransaction().begin();
        getEntityManager().remove(test);
        getEntityManager().getTransaction().commit();
        final FieldAccessAnnotationTestBean shouldNotExists =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerCommitting() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        getEntityManager().persist(child);
        getEntityManager().getTransaction().commit();
        getEntityManager().getTransaction().begin();
        getEntityManager().createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        getEntityManager().getTransaction().commit();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(test, fieldAccessAnnotationTestBean);
        assertNotNull(fieldAccessAnnotationTestBean);
        getEntityManager().getTransaction().begin();
        getEntityManager().remove(getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        getEntityManager().getTransaction().commit();
        final FieldAccessAnnotationTestBean shouldNotExists =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityCreatedAndLoadedWithinSameLongRunningEntityManagerFlushing() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().flush();
        final FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("child", test);
        getEntityManager().persist(child);
        getEntityManager().flush();
        getEntityManager().createQuery("delete from FieldAccessAnnotationTestBean c where c.parent.id=:parentId")
            .setParameter("parentId", test.getIdentifier()).executeUpdate();
        getEntityManager().flush();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotNull(fieldAccessAnnotationTestBean);
        assertSame(test, fieldAccessAnnotationTestBean);
        getEntityManager().flush();
        getEntityManager().remove(getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        getEntityManager().flush();
        final FieldAccessAnnotationTestBean shouldNotExists =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNull(shouldNotExists);
    }

    @Test
    public void removeEntityLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        closeEntityManager();
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        getEntityManager().remove(fieldAccessAnnotationTestBean2);
        getEntityManager().getTransaction().commit();
        getEntityManager().getTransaction().begin();
        assertNull(
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        getEntityManager().getTransaction().commit();
    }

    @Test
    public void
    removeEntityWithoutExplicitFlushingLoadedAlreadyWithinCurrentEntityManagerExpectFindReturnsNullAfterRemove() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        closeEntityManager();
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        getEntityManager().remove(fieldAccessAnnotationTestBean2);
        assertNull(
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier()));
        getEntityManager().getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectIsSameWithinOneEntityManager() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        closeEntityManager();
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            getEntityManager().getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
        getEntityManager().getTransaction().commit();
    }

    @Test
    public void getReferenceWithinEntityManagerExpectNotSameAfterRemove() {
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean test = new FieldAccessAnnotationTestBean("test");
        getEntityManager().persist(test);
        getEntityManager().getTransaction().commit();
        closeEntityManager();
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean1 =
            getEntityManager().find(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        getEntityManager().remove(fieldAccessAnnotationTestBean1);
        getEntityManager().getTransaction().commit();
        final FieldAccessAnnotationTestBean fieldAccessAnnotationTestBean2 =
            getEntityManager().getReference(FieldAccessAnnotationTestBean.class, test.getIdentifier());
        assertNotSame(fieldAccessAnnotationTestBean1, fieldAccessAnnotationTestBean2);
    }
}
