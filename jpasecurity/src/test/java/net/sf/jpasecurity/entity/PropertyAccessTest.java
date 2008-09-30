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
package net.sf.jpasecurity.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class PropertyAccessTest extends TestCase {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    
    public void testOneToManyNavigation() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER1);
        bean.getChildBeans().add(new FieldAccessAnnotationTestBean(USER1));
        bean.getChildBeans().add(new FieldAccessAnnotationTestBean(USER2));
        bean = entityManager.merge(bean);
        entityManager.getTransaction().commit();
        entityManager.close();
        assertEquals(1, bean.getChildBeans().size());
        assertEquals(USER1, bean.getChildBeans().get(0).getBeanName());
        assertEquals(2, ((SecureList<FieldAccessAnnotationTestBean>)bean.getChildBeans()).getOriginal().size());
    }
    
    public void testUpdate() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(bean);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        bean.setBeanName(USER2);
        entityManager.getTransaction().commit();
        entityManager.close();
        
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
            fail("should have thrown exception, since we are not allowed to see beans with name " + USER2);
        } catch (SecurityException e) {
            //expected
        }
        entityManager.getTransaction().rollback();
        entityManager.close();

        TestAuthenticationProvider.authenticate(USER2);
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        entityManager.getTransaction().commit();
        entityManager.close();
        assertEquals(USER2, bean.getBeanName());
    }
    
    public void tearDown() {
        TestAuthenticationProvider.authenticate(null);        
    }
}
