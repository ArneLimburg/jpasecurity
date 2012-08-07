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
import java.util.HashMap;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaSecurityUnit;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.model.acl.AbstractAclProtectedEntity;
import net.sf.jpasecurity.model.acl.AbstractEntity;
import net.sf.jpasecurity.model.acl.AclProtectedEntity;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.SecondAclProtectedEntity;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import net.sf.jpasecurity.persistence.JpaExceptionFactory;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;
import net.sf.jpasecurity.security.authentication.DefaultAuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesCompiler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @author Arne Limburg */
public class JpaInheritenceEntityFilterTest {

    private MappingInformation mappingInformation;
    private Collection<AccessRule> accessRules;
    private EntityFilter filter;

    @Before
    public void initialize() throws Exception {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(AbstractAclProtectedEntity.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(AclProtectedEntity.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(SecondAclProtectedEntity.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(AbstractEntity.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(Group.class.getName());
        SecurityUnit securityUnitInformation = new JpaSecurityUnit(persistenceUnitInfo);
        mappingInformation = new OrmXmlParser(securityUnitInformation, new JpaExceptionFactory()).parse();
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule(
                "GRANT ACCESS TO net.sf.jpasecurity.model.acl.AccessControlled bean "
                    + "WHERE (bean.accessControlList is null) OR (bean.accessControlList=CURRENT_PRINCIPAL))"
            );
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation, new DefaultExceptionFactory());
        accessRules = compiler.compile(rule);
        SecureObjectManager secureObjectManager = createMock(SecureObjectManager.class);
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        SecurityContext securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
        expect(secureObjectManager.getSecureObjects((Class<Object>)anyObject()))
            .andReturn(Collections.<Object>emptySet()).anyTimes();
        replay(secureObjectManager);
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
        filter = new EntityFilter(secureObjectManager,
            mappingInformation,
            securityContext,
            exceptionFactory,
            accessRules);
    }

    @Test
    public void checkStatementForConcreteSubtype() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), SecondAclProtectedEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ);
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals("(((i.accessControlList IS  NULL ) OR (i.accessControlList = :CURRENT_PRINCIPAL)))",
            accessRules1.toString());
    }

    @Test
    public void checkStatementForProtectedBaseSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractAclProtectedEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ);
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals("(((i.accessControlList IS  NULL ) OR (i.accessControlList = :CURRENT_PRINCIPAL)))",
            accessRules1.toString());
    }

    @Test
    public void checkStatementForMixedConcreteTypes() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), SecondAclProtectedEntity.class);
        selectedTypes.put(new Path("b"), AclProtectedEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ);
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals("(((i.accessControlList IS  NULL ) OR (i.accessControlList = :CURRENT_PRINCIPAL)) "
                + "AND ((b.accessControlList IS  NULL ) OR (b.accessControlList = :CURRENT_PRINCIPAL)))",
            accessRules1.toString());
    }

    @Test
    public void checkStatementForNotProtectedSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ);
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals(
            "(( EXISTS "
                + "( SELECT abstractAclProtectedEntity FROM AbstractAclProtectedEntity abstractAclProtectedEntity "
                + "WHERE abstractAclProtectedEntity = i)  AND ((i.accessControlList IS  NULL ) "
                + "OR (i.accessControlList = :CURRENT_PRINCIPAL)) "
                + "OR  NOT  EXISTS ( SELECT abstractAclProtectedEntity "
                + "FROM AbstractAclProtectedEntity abstractAclProtectedEntity WHERE abstractAclProtectedEntity = i) ))",
            accessRules1.toString());
    }

    @Test
    public void checkStatementForMultipleNotProtectedSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractEntity.class);
        selectedTypes.put(new Path("b"), AbstractEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ);
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals(
            "(( EXISTS ( SELECT abstractAclProtectedEntity FROM AbstractAclProtectedEntity abstractAclProtectedEntity"
                + " WHERE abstractAclProtectedEntity = i)  AND ((i.accessControlList IS  NULL ) "
                + "OR (i.accessControlList = :CURRENT_PRINCIPAL)) "
                + "OR  NOT  EXISTS ( SELECT abstractAclProtectedEntity FROM AbstractAclProtectedEntity "
                + "abstractAclProtectedEntity WHERE abstractAclProtectedEntity = i) ) AND ( EXISTS ( SELECT "
                + "abstractAclProtectedEntity FROM AbstractAclProtectedEntity abstractAclProtectedEntity "
                + "WHERE abstractAclProtectedEntity = b)  AND ((b.accessControlList IS  NULL ) OR "
                + "(b.accessControlList = :CURRENT_PRINCIPAL)) OR  NOT  EXISTS ( SELECT abstractAclProtectedEntity"
                + " FROM AbstractAclProtectedEntity abstractAclProtectedEntity "
                + "WHERE abstractAclProtectedEntity = b) ))",
            accessRules1.toString());
    }
}
