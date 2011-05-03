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
package net.sf.jpasecurity.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class LightEntityManagerTest {

    private static final String USER1 = "user1";
    private static final String USER2 = "user2";

    @Test
    public void query() {
        TestAuthenticationProvider.authenticate(USER1);
        Map<String, String> persistenceProperties
            = Collections.singletonMap(SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY,
                                       SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access", persistenceProperties);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(bean);
        entityManager.getTransaction().commit();
        entityManager.close();

        TestAuthenticationProvider.authenticate(null);
        entityManager = entityManagerFactory.createEntityManager();
        assertNotNull("entity may be found by id in light entity-managers",
                      entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier()));
        Query query
            = entityManager.createQuery("select bean from FieldAccessAnnotationTestBean bean where bean.id = ?0");
        query.setParameter(0, bean.getIdentifier());
        assertEquals(0, query.getResultList().size());
        entityManager.close();
    }

    @Test
    public void relations() {
        TestAuthenticationProvider.authenticate("root", "admin");
        Map<String, String> persistenceProperties
            = Collections.singletonMap(SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY,
                                       SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access", persistenceProperties);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER1);
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER2);
        parent.getChildBeans().add(child);
        child.setParentBean(parent);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();
        entityManager.close();

        TestAuthenticationProvider.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        Query query
            = entityManager.createQuery("select bean from FieldAccessAnnotationTestBean bean where bean.id = ?0");
        query.setParameter(0, parent.getIdentifier());
        FieldAccessAnnotationTestBean foundParent = (FieldAccessAnnotationTestBean)query.getSingleResult();
        //child may be accessed in light entity-managers
        FieldAccessAnnotationTestBean foundChild = foundParent.getChildBeans().iterator().next();
        assertEquals("wrong child from light entity-managers", child.getIdentifier(), foundChild.getIdentifier());
        entityManager.close();
    }
}
