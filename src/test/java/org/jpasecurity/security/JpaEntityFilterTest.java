/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Arne Limburg
 */
public class JpaEntityFilterTest {

    private Metamodel metamodel;
    private DefaultAccessManager accessManager;
    private Collection<AccessRule> accessRules;

    @Before
    public void initialize() throws Exception {
        metamodel = mock(Metamodel.class);
        EntityType contactType = mock(EntityType.class);
        SingularAttribute ownerAttribute = mock(SingularAttribute.class);
        accessManager = mock(DefaultAccessManager.class);
        when(accessManager.getContext()).thenReturn(new DefaultSecurityContext());
        when(contactType.getName()).thenReturn(Contact.class.getSimpleName());
        when(contactType.getJavaType()).thenReturn((Class)Contact.class);
        when(metamodel.getEntities())
            .thenReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(contactType)));
        when(metamodel.entity(Contact.class)).thenReturn(contactType);
        when(metamodel.managedType(Contact.class)).thenReturn(contactType);
        when(contactType.getAttributes()).thenReturn(Collections.singleton(ownerAttribute));
        when(contactType.getAttribute("owner")).thenReturn(ownerAttribute);
        when(ownerAttribute.getName()).thenReturn("owner");
        when(ownerAttribute.getJavaMember()).thenReturn(Contact.class.getDeclaredField("owner"));

        DefaultAccessManager.Instance.register(accessManager);

        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule("GRANT READ ACCESS TO Contact contact WHERE contact.owner = CURRENT_PRINCIPAL");
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);
        accessRules = compiler.compile(rule);
    }

    @After
    public void removeAccessManager() {
        DefaultAccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void access() throws Exception {
        DefaultSecurityContext securityContext
            = (DefaultSecurityContext)DefaultAccessManager.Instance.get().getContext();
        SecurePersistenceUnitUtil persistenceUnitUtil = mock(SecurePersistenceUnitUtil.class);
        when(persistenceUnitUtil.isLoaded(any())).thenReturn(true);
        when(persistenceUnitUtil.initialize(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        });

        EntityFilter filter = new EntityFilter(metamodel, persistenceUnitUtil, accessRules);
        User john = new User("John");
        Contact contact = new Contact(john, "123456789");

        securityContext.register(new Alias("CURRENT_PRINCIPAL"), john);
        assertTrue(filter.isAccessible(AccessType.READ, contact));
        securityContext.register(new Alias("CURRENT_PRINCIPAL"), new User("Mary"));
        assertFalse(filter.isAccessible(AccessType.READ, contact));
    }
}
