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
package net.sf.jpasecurity.security;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import net.sf.jpasecurity.persistence.JpaExceptionFactory;
import net.sf.jpasecurity.persistence.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.security.authentication.DefaultAuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesCompiler;

/**
 * @author Arne Limburg
 */
public class EntityFilterTest extends TestCase {

    private MappingInformation mappingInformation;
    private List<AccessRule> accessRules;
    
    public void setUp() throws Exception {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(Contact.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(User.class.getName());
        mappingInformation = new JpaAnnotationParser(new JpaExceptionFactory()).parse(persistenceUnitInfo);
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule("GRANT READ ACCESS TO Contact contact WHERE contact.owner = CURRENT_PRINCIPAL");
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation, new DefaultExceptionFactory());
        accessRules = Collections.singletonList(compiler.compile(rule));
    }
    
    public void testIsAccessible() throws Exception {
        EntityManager entityManager = createMock(EntityManager.class);
        SecureObjectManager secureObjectManager = createMock(SecureObjectManager.class);
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        SecurityContext securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
        expect(secureObjectManager.getSecureObjects((Class<Object>)anyObject()))
            .andReturn((Collection<Object>)Collections.EMPTY_SET).anyTimes();
        replay(entityManager, secureObjectManager);
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
        EntityFilter filter = new EntityFilter(secureObjectManager,
                                               mappingInformation,
                                               new MappedPathEvaluator(mappingInformation, exceptionFactory),
                                               exceptionFactory,
                                               accessRules);
        User john = new User("John");
        Contact contact = new Contact(john, "123456789");
        
        authenticationProvider.authenticate(john);
        assertTrue(filter.isAccessible(contact, AccessType.READ, securityContext));
        authenticationProvider.authenticate(new User("Mary"));
        assertFalse(filter.isAccessible(contact, AccessType.READ, securityContext));
    }
}
