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
package org.jpasecurity.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jpasecurity.model.MethodAccessAnnotationTestBean;
import org.jpasecurity.model.acl.PrivilegeType;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Arne Limburg
 */
@Ignore
public class QueryTest {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";

    @Test
    public void testEmptyResult() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("xml-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from FieldAccessXmlTestBean bean");
        assertTrue(query instanceof EmptyResultQuery);
        assertEquals(0, query.getResultList().size());
    }

    @Test
    public void enumParameter() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from Privilege bean WHERE bean.type=:TYPE");
        query.setParameter("TYPE", PrivilegeType.DATA);
        query.getResultList();
    }

    @Test
    public void enumParameterList() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from Privilege bean WHERE bean.type in (:TYPES)");
        final ArrayList<PrivilegeType> types = new ArrayList<PrivilegeType>();
        types.add(PrivilegeType.DATA);
        types.add(PrivilegeType.METHOD);
        query.setParameter("TYPES", types);
        query.getResultList();
    }

    @Test
    public void scalarResult() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-method-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestSecurityContext.authenticate("root", "admin");
        ParentChildTestData testData = new ParentChildTestData(entityManager);
        MethodAccessAnnotationTestBean child1 = testData.createPermutations(USER1, USER2).iterator().next();
        MethodAccessAnnotationTestBean parent1 = (MethodAccessAnnotationTestBean)child1.getParent();
        entityManager.getTransaction().commit();
        entityManager.close();

        TestSecurityContext.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Object[]> result
            = entityManager.createQuery("select bean.name, bean.parent from MethodAccessAnnotationTestBean bean")
                           .getResultList();
        assertEquals(1, result.size());
        assertEquals(USER1, result.get(0)[0]);
        assertEquals(parent1, result.get(0)[1]);
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void hibernateWithClause() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-method-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestSecurityContext.authenticate("root", "admin");
        ParentChildTestData testData = new ParentChildTestData(entityManager);
        MethodAccessAnnotationTestBean child1 = testData.createPermutations(USER1, USER2).iterator().next();
        entityManager.getTransaction().commit();
        entityManager.close();

        TestSecurityContext.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<MethodAccessAnnotationTestBean> result
            = entityManager.createQuery("select bean from MethodAccessAnnotationTestBean bean "
                                        + "join bean.parent parent with parent.name = '" + USER1 + "' "
                                        + "where bean.name = :name")
                           .setParameter("name", USER1)
                           .getResultList();
        assertEquals(1, result.size());
        assertEquals(child1, result.iterator().next());
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
        TestSecurityContext.authenticate(null);
    }
}
