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
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Stefan Hildebrandt
 */
public class NewEntityTest {

    public static final String USER1 = "user1";

    @Test
    @Ignore("support for only one constructor argument is broken")
    public void oneConstructorArgument() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        final Query query = entityManager.createQuery(
            "select new net.sf.jpasecurity.model.FieldAccessAnnotationTestBean(faatb.name) "
                + "from FieldAccessAnnotationTestBean faatb");
        final List resultList = query.getResultList();
        Assert.assertEquals(1, resultList.size());
        final Object result = resultList.get(0);
        Assert.assertEquals(FieldAccessAnnotationTestBean.class, result.getClass());
        final FieldAccessAnnotationTestBean actual = (FieldAccessAnnotationTestBean)result;
        Assert.assertEquals(USER1, actual.getBeanName());
        entityManager.close();
    }

    @Test
    public void moreConstructorArgumentsWithEntity() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        final Query query = entityManager.createQuery(
            "select new net.sf.jpasecurity.model.FieldAccessAnnotationTestBean(faatb.name, faatb) "
                + "from FieldAccessAnnotationTestBean faatb");
        final List resultList = query.getResultList();
        Assert.assertEquals(1, resultList.size());
        final Object result = resultList.get(0);
        Assert.assertEquals(FieldAccessAnnotationTestBean.class, result.getClass());
        final FieldAccessAnnotationTestBean actual = (FieldAccessAnnotationTestBean)result;
        Assert.assertEquals(USER1, actual.getBeanName());
        Assert.assertEquals(parent.getIdentifier(), actual.getParentBean().getIdentifier());
        entityManager.close();
    }

    //TODO tests for security checks needed???
}
