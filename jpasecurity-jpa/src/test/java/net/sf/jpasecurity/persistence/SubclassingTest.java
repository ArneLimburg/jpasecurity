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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jpasecurity.model.AbstractEntity;
import net.sf.jpasecurity.model.AbstractSuperclass;
import net.sf.jpasecurity.model.Subclass1;
import net.sf.jpasecurity.model.SuperclassReferencingBean;
import net.sf.jpasecurity.model.TestBean;
import net.sf.jpasecurity.model.TestBeanSubclass;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SubclassingTest {

    public static final String USER = "user";

    private EntityManagerFactory factory;

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(USER);
        factory = Persistence.createEntityManagerFactory("subclassing-test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        TestBean testBean = new TestBean();
        entityManager.persist(testBean);
        TestBeanSubclass testBeanSubclass = new TestBeanSubclass(USER);
        entityManager.persist(testBeanSubclass);
        testBean.setParent(testBeanSubclass);
        AbstractEntity subclass = new Subclass1();
        entityManager.persist(subclass);
        entityManager.persist(new SuperclassReferencingBean(subclass));
        entityManager.getTransaction().commit();
        entityManager.close();
        TestAuthenticationProvider.authenticate(null);
    }

    @After
    public void closeEntityManagerFactory() {
        factory.close();
    }

    @Test
    public void accessRulesOnSubclasses() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        TestAuthenticationProvider.authenticate(USER);
        assertEquals(2, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void accessRulesOnSubclassesWithGenericSuperclass() {
        EntityManager entityManager = factory.createEntityManager();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM Subclass1 bean").getResultList().size());
        entityManager.close();
    }

    @Test
    public void referenceToSuperclass() {
        EntityManager entityManager = factory.createEntityManager();
        AbstractSuperclass<Integer> superclass = entityManager.find(SuperclassReferencingBean.class, 1).getSuperclass();
        entityManager.close();
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(superclass);
        entityManager.getTransaction().commit();
        entityManager.close();
    }


    @Test
    public void testIdentity() {
        TestAuthenticationProvider.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        TestBean child
            = entityManager.createQuery("SELECT bean FROM TestBean bean WHERE bean.parent IS NOT NULL", TestBean.class)
                .getSingleResult();
        assertSame(child.getParent(), entityManager.find(TestBean.class, child.getParent().getId()));
        TestBean parentFromQuery
            = entityManager.createQuery("SELECT bean FROM TestBean bean WHERE id=:id",
                TestBean.class).setParameter("id", child.getParent().getId())
                .getSingleResult();
        assertSame(child.getParent(), parentFromQuery);
        TestBean parentFromQuerySubType
            = entityManager.createQuery("SELECT bean FROM TestBeanSubclass bean WHERE id=:id",
                TestBeanSubclass.class).setParameter("id", child.getParent().getId())
                .getSingleResult();
        assertSame(child.getParent(), parentFromQuerySubType);
        entityManager.close();
    }

    @Test
    @Ignore("Is not compatible with ObjectIdentityTest")
    public void loadingOfRelatedSubclass() {
        TestAuthenticationProvider.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        TestBean child
            = entityManager.createQuery("SELECT bean FROM TestBean bean WHERE bean.parent IS NOT NULL", TestBean.class)
                .getSingleResult();
        assertFalse(child.getParent() instanceof TestBeanSubclass);
        assertTrue(entityManager.find(TestBean.class, child.getParent().getId()) instanceof TestBeanSubclass);
        entityManager.close();
    }
}
