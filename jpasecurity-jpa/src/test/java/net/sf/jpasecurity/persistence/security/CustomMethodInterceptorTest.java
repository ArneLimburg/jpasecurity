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
package net.sf.jpasecurity.persistence.security;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.entity.AbstractSecureObjectManager;
import net.sf.jpasecurity.entity.SecureEntityInterceptor;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.proxy.SuperMethod;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CustomMethodInterceptorTest {

    private static final int FULL_ACCESS_PRIVILEGE = -1;
    private static final int NO_ACCESS_PRIVILEGE = 1;
    private static EntityManagerFactory entityManagerFactory;

    @BeforeClass
    public static void createEntityManagerFactory() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(Configuration.METHOD_INTERCEPTOR_PROPERTY, TestInterceptor.class.getName());
        entityManagerFactory = Persistence.createEntityManagerFactory("interceptor", properties);
    }

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(FULL_ACCESS_PRIVILEGE, FULL_ACCESS_PRIVILEGE);
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        User user = new User("user");
        entityManager.persist(user);
        Contact contact = new Contact(user, "contact");
        entityManager.persist(contact);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void testCustomMethodInterceptor() {
        TestAuthenticationProvider.authenticate(NO_ACCESS_PRIVILEGE, NO_ACCESS_PRIVILEGE);
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        final Contact contact =
            entityManager.createQuery(
                "select contact from Contact contact where contact.text = 'contact'",
                Contact.class)
                .getSingleResult();

        assertTrue(contact.getOwner() instanceof SecureEntity);
        final SecureEntity entity = (SecureEntity)contact.getOwner();
        assertFalse(entity.isAccessible(AccessType.READ));
        assertEquals("user", contact.getOwner().getName());
    }

    public class TestInterceptor extends SecureEntityInterceptor {

        private final Object entity;

        public TestInterceptor(BeanInitializer beanInitializer,
                               AbstractSecureObjectManager objectManager, Object entity) {
            super(beanInitializer, objectManager, entity);
            this.entity = entity;
        }

        @Override
        public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args)
            throws Throwable {
            if ("getName".equals(method.getName())) {
                return method.invoke(entity, args);
            }
            return super.intercept(object, method, superMethod, args);
        }
    }
}


