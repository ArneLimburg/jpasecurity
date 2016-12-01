/*
 * Copyright 2008 Arne Limburg
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

import java.util.Collection;
import java.util.Collections;

import org.jpasecurity.AccessType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.configuration.AccessRule;
import org.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import org.jpasecurity.configuration.DefaultExceptionFactory;
import org.jpasecurity.configuration.SecurityContext;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.jpasecurity.entity.SecureObjectManager;
import org.jpasecurity.jpa.JpaSecurityUnit;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import org.jpasecurity.persistence.JpaExceptionFactory;
import org.jpasecurity.persistence.mapping.OrmXmlParser;
import org.jpasecurity.security.authentication.DefaultAuthenticationProvider;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class JpaEntityFilterTest {

    private MappingInformation mappingInformation;
    private Collection<AccessRule> accessRules;

    @Before
    public void initialize() throws Exception {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(Contact.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(User.class.getName());
        SecurityUnit securityUnitInformation = new JpaSecurityUnit(persistenceUnitInfo);
        mappingInformation = new OrmXmlParser(securityUnitInformation, new JpaExceptionFactory()).parse();
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule("GRANT READ ACCESS TO Contact contact WHERE contact.owner = CURRENT_PRINCIPAL");
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation, new DefaultExceptionFactory());
        accessRules = compiler.compile(rule);
    }

    @Test
    public void access() throws Exception {
        SecureObjectManager secureObjectManager = createMock(SecureObjectManager.class);
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        SecurityContext securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
        expect(secureObjectManager.getSecureObjects((Class<Object>)anyObject()))
            .andReturn(Collections.<Object>emptySet()).anyTimes();
        replay(secureObjectManager);
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
        EntityFilter filter = new EntityFilter(secureObjectManager,
                                               mappingInformation,
                                               securityContext,
                                               exceptionFactory,
                                               accessRules);
        User john = new User("John");
        Contact contact = new Contact(john, "123456789");

        authenticationProvider.authenticate(john);
        assertTrue(filter.isAccessible(AccessType.READ, contact));
        authenticationProvider.authenticate(new User("Mary"));
        assertFalse(filter.isAccessible(AccessType.READ, contact));
    }
}
