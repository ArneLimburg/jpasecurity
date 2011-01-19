/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.security;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class AccessCheckTest extends TestCase {

    private static final String CREATOR = "creator";
    private static final String USER = "user";
    private static final String ADMIN = "admin";
    private static final String CHILD = "child";
    private static final String GRANDCHILD = "grandchild";

    public void testCreate() {
        TestAuthenticationProvider.authenticate(CREATOR, CREATOR);
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
        assertEquals(1, bean.getChildBeans().size());
        assertEquals(CHILD, bean.getChildBeans().iterator().next().getBeanName());
        assertEquals(1, child.getChildBeans().size());
        assertEquals(GRANDCHILD, child.getChildBeans().iterator().next().getBeanName());
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

    public void testUpdate() {
        TestAuthenticationProvider.authenticate(ADMIN, ADMIN);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("access-check");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        entityManager.getTransaction().commit();
        entityManager.close();

        TestAuthenticationProvider.authenticate(USER);
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        try {
            bean.setBeanName("new BeanName");
            entityManager.getTransaction().commit();
            fail("expected security exception");
        } catch (SecurityException e) {
            // expected
            entityManager.getTransaction().rollback();
        }
        entityManager.close();
    }
}
