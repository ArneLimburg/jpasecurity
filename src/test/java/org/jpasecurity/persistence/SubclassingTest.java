/*
 * Copyright 2008 - 2018 Arne Limburg
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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jpasecurity.model.AbstractEntity;
import org.jpasecurity.model.AbstractSuperclass;
import org.jpasecurity.model.Bean;
import org.jpasecurity.model.BeanSubclass;
import org.jpasecurity.model.Subclass1;
import org.jpasecurity.model.SuperclassReferencingBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SubclassingTest {

    private static final String USER = "user";
    private static final String USER_CRITERIA = "user_criteria";

    private EntityManagerFactory factory;

    private SuperclassReferencingBean superclassReferencingBean;

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);
        factory = Persistence.createEntityManagerFactory("subclassing-test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        Bean testBean = new Bean();
        entityManager.persist(testBean);

        TestSecurityContext.authenticate(USER);
        BeanSubclass testBeanSubclass = new BeanSubclass(USER);
        entityManager.persist(testBeanSubclass);
        testBean.setParent(testBeanSubclass);
        AbstractEntity subclass = new Subclass1();
        entityManager.persist(subclass);
        superclassReferencingBean = new SuperclassReferencingBean(subclass);
        entityManager.persist(superclassReferencingBean);

        TestSecurityContext.authenticate(USER_CRITERIA);
        BeanSubclass criteriaTestBeanSubclass = new BeanSubclass(USER_CRITERIA);
        entityManager.persist(criteriaTestBeanSubclass);

        entityManager.getTransaction().commit();
        entityManager.close();
        TestSecurityContext.authenticate(null);
    }

    @After
    public void closeEntityManagerFactory() {
        factory.close();
    }

    @Test
    public void accessRulesOnSubclasses() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM Bean bean").getResultList().size());
        TestSecurityContext.authenticate(USER);
        assertEquals(2, entityManager.createQuery("SELECT bean FROM Bean bean").getResultList().size());
        TestSecurityContext.authenticate(USER_CRITERIA);
        assertEquals(2, entityManager.createQuery("SELECT bean FROM Bean bean").getResultList().size());
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    //TODO
    @Ignore
    @Test
    public void accessRulesOnSubclassesWithGenericSuperclass() {
        EntityManager entityManager = factory.createEntityManager();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM Subclass1 bean").getResultList().size());
        entityManager.close();
    }

    @Test
    public void referenceToSuperclass() {
        EntityManager entityManager = factory.createEntityManager();
        AbstractSuperclass<Integer> superclass
            = entityManager.find(SuperclassReferencingBean.class, superclassReferencingBean.getId()).getSuperclass();
        entityManager.close();
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.merge(superclass);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void accessRulesOnSubclassesWithCriteria() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        TestSecurityContext.authenticate(USER_CRITERIA);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bean> query = criteriaBuilder.createQuery(Bean.class);
        Root<Bean> readAccessUser = query.from(Bean.class);
        CriteriaQuery<Bean> selectStatement = query.select(readAccessUser);
        List<Bean> resultList = entityManager.createQuery(selectStatement).getResultList();

        assertEquals(2, resultList.size());

        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
