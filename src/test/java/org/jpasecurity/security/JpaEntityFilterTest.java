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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class JpaEntityFilterTest {

    private Metamodel metamodel;
    private AccessManager accessManager;
    private Collection<AccessRule> accessRules;

    @Before
    public void initialize() throws Exception {
        metamodel = createMock(Metamodel.class);
        EntityType contactType = createMock(EntityType.class);
        SingularAttribute ownerAttribute = createMock(SingularAttribute.class);
        accessManager = createMock(AccessManager.class);
        expect(accessManager.getContext()).andReturn(new DefaultSecurityContext()).anyTimes();
        expect(contactType.getName()).andReturn(Contact.class.getSimpleName()).anyTimes();
        expect(contactType.getJavaType()).andReturn((Class)Contact.class).anyTimes();
        expect(metamodel.getEntities())
            .andReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(contactType))).anyTimes();
        expect(metamodel.entity(Contact.class)).andReturn(contactType).anyTimes();
        expect(metamodel.managedType(Contact.class)).andReturn(contactType).anyTimes();
        expect(contactType.getAttributes()).andReturn(Collections.singleton(ownerAttribute)).anyTimes();
        expect(contactType.getAttribute("owner")).andReturn(ownerAttribute).anyTimes();
        expect(ownerAttribute.getName()).andReturn("owner").anyTimes();
        expect(ownerAttribute.getJavaMember()).andReturn(Contact.class.getDeclaredField("owner")).anyTimes();
        replay(metamodel, contactType, ownerAttribute, accessManager);
        AccessManager.Instance.register(accessManager);

        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule("GRANT READ ACCESS TO Contact contact WHERE contact.owner = CURRENT_PRINCIPAL");
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);
        accessRules = compiler.compile(rule);
    }

    @After
    public void removeAccessManager() {
        AccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void access() throws Exception {
        DefaultSecurityContext securityContext = (DefaultSecurityContext)AccessManager.Instance.get().getContext();
        PersistenceUnitUtil persistenceUnitUtil = createMock(PersistenceUnitUtil.class);
        expect(persistenceUnitUtil.isLoaded(anyObject())).andReturn(true).anyTimes();
        replay(persistenceUnitUtil);
        EntityFilter filter = new EntityFilter(metamodel, persistenceUnitUtil, accessRules);
        User john = new User("John");
        Contact contact = new Contact(john, "123456789");

        securityContext.register(new Alias("CURRENT_PRINCIPAL"), john);
        assertTrue(filter.isAccessible(AccessType.READ, contact));
        securityContext.register(new Alias("CURRENT_PRINCIPAL"), new User("Mary"));
        assertFalse(filter.isAccessible(AccessType.READ, contact));
    }
}
