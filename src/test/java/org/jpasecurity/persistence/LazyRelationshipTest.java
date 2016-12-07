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

import javax.persistence.EntityManager;

import org.jpasecurity.model.TestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author Arne Limburg
 */
public class LazyRelationshipTest extends AbstractEntityTestCase {

    public static final String USER = "user";

    private int childId;
    private int parentId;

    @BeforeClass
    public static void createEntityManagerFactory() {
        TestSecurityContext.authenticate(USER);
        createEntityManagerFactory("lazy-relationship");
    }

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        TestBean testBean = new TestBean(USER);
        entityManager.persist(testBean);
        TestBean child = new TestBean();
        child.setParent(testBean);
        entityManager.persist(child);
        entityManager.getTransaction().commit();
        closeEntityManager();
        TestSecurityContext.authenticate(null);
        childId = child.getId();
        parentId = testBean.getId();
    }

    @After
    public void unauthenticate() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void accessChild() {
        TestSecurityContext.authenticate(USER);
        createEntityManager();
        getEntityManager().find(TestBean.class, childId);
    }

    @Test
    public void testFlushBeforeFind() {
        TestSecurityContext.authenticate(USER);
        createEntityManager();
        getEntityManager().getTransaction().begin();
        final TestBean child = getEntityManager().find(TestBean.class, childId);
        getEntityManager().find(TestBean.class, parentId);
        getEntityManager().flush();
        getEntityManager().getTransaction().rollback();
        assertFalse(child.isPreUpdate());
    }
}
