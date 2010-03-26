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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class QueryTest extends TestCase {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    
    public void testScalarResult() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("annotation-based-method-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestAuthenticationProvider.authenticate("root", "admin");
        MethodAccessAnnotationTestBean child1 = createChild(USER1, USER1);
        MethodAccessAnnotationTestBean parent1 = child1.getParent();
        MethodAccessAnnotationTestBean child2 = createChild(USER1, USER2);
        MethodAccessAnnotationTestBean parent2 = child2.getParent();
        MethodAccessAnnotationTestBean child3 = createChild(USER2, USER1);
        MethodAccessAnnotationTestBean parent3 = child3.getParent();
        MethodAccessAnnotationTestBean child4 = createChild(USER2, USER2);
        MethodAccessAnnotationTestBean parent4 = child4.getParent();
        entityManager.persist(child1);
        entityManager.persist(parent1);
        entityManager.persist(child2);
        entityManager.persist(parent2);
        entityManager.persist(child3);
        entityManager.persist(parent3);
        entityManager.persist(child4);
        entityManager.persist(parent4);
        entityManager.getTransaction().commit();
        entityManager.close();
        
        TestAuthenticationProvider.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Object[]> result
            = entityManager.createQuery("select bean.name, bean.parent from MethodAccessAnnotationTestBean bean").getResultList();
        assertEquals(1, result.size());
        assertEquals(USER1, result.get(0)[0]);
        assertEquals(parent1, result.get(0)[1]);
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
        TestAuthenticationProvider.authenticate(null);
    }
    
    private MethodAccessAnnotationTestBean createChild(String parentName, String childName) {
        MethodAccessAnnotationTestBean parent = new MethodAccessAnnotationTestBean(parentName);
        MethodAccessAnnotationTestBean child = new MethodAccessAnnotationTestBean(childName);
        child.setParent(parent);
        parent.getChildren().add(child);
        return child;
    }
}
