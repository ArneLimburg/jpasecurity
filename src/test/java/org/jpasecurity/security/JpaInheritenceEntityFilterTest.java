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

import java.util.Collection;
import java.util.HashMap;

import org.jpasecurity.AccessType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.configuration.AccessRule;
import org.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import org.jpasecurity.configuration.DefaultExceptionFactory;
import org.jpasecurity.configuration.SecurityContext;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.Path;
import org.jpasecurity.model.acl.AbstractAclProtectedEntity;
import org.jpasecurity.model.acl.AbstractEntity;
import org.jpasecurity.model.acl.AclProtectedEntity;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.SecondAclProtectedEntity;
import org.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import org.jpasecurity.persistence.JpaExceptionFactory;
import org.jpasecurity.persistence.mapping.OrmXmlParser;
import org.jpasecurity.security.authentication.DefaultAuthenticationProvider;
import org.jpasecurity.security.rules.AccessRulesCompiler;
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
        mappingInformation = new OrmXmlParser(persistenceUnitInfo, new JpaExceptionFactory()).parse();
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule(
                "GRANT ACCESS TO org.jpasecurity.model.acl.AccessControlled bean "
                    + "WHERE (bean.accessControlList is null) OR (bean.accessControlList=CURRENT_PRINCIPAL))"
            );
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation, new DefaultExceptionFactory());
        accessRules = compiler.compile(rule);
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        SecurityContext securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
        filter = new EntityFilter(mappingInformation,
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
