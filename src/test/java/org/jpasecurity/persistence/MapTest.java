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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.FieldAccessMapKey;
import org.jpasecurity.model.FieldAccessMapValue;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class MapTest {

    public static final String USER1 = "user1";

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void mapMapping() {
        TestSecurityContext.authenticate(USER1);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER1);
        FieldAccessMapKey key = new FieldAccessMapKey(USER1);
        FieldAccessMapValue value = new FieldAccessMapValue(key, parent);
        parent.getValues().put(key, value);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        FieldAccessAnnotationTestBean bean
            = entityManager.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        assertEquals(1, bean.getValues().size());
        assertEquals(key, bean.getValues().keySet().iterator().next());
        assertEquals(value, bean.getValues().values().iterator().next());
        assertEquals(bean, bean.getValues().values().iterator().next().getParent());
        entityManager.close();
    }
}
