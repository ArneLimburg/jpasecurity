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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessMapKey;
import net.sf.jpasecurity.model.FieldAccessMapValue;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class MapTest extends TestCase {

    public static final String USER1 = "user1";

    public void testMapMapping() {
        TestAuthenticationProvider.authenticate(USER1);
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
