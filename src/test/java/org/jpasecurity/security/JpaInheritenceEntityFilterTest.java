/*
 * Copyright 2008 - 2017 Arne Limburg
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.model.acl.AbstractAclProtectedEntity;
import org.jpasecurity.model.acl.AbstractEntity;
import org.jpasecurity.model.acl.AclProtectedEntity;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.SecondAclProtectedEntity;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/** @author Arne Limburg */
public class JpaInheritenceEntityFilterTest {

    private Metamodel metamodel;
    private AccessManager accessManager;
    private Collection<AccessRule> accessRules;
    private EntityFilter filter;

    @Before
    public void initialize() throws Exception {
        metamodel = createMock(Metamodel.class);
        MappedSuperclassType abstractAclProtectedEntityType = createMock(MappedSuperclassType.class);
        EntityType aclProtectedEntityType = createMock(EntityType.class);
        EntityType secondAclProtectedEntityType = createMock(EntityType.class);
        MappedSuperclassType abstractEntityType = createMock(MappedSuperclassType.class);
        EntityType groupType = createMock(EntityType.class);
        PersistenceUnitUtil persistenceUnitUtil = createMock(PersistenceUnitUtil.class);
        accessManager = createMock(AccessManager.class);
        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.register(new Alias("CURRENT_PRINCIPAL"), "user");
        expect(accessManager.getContext()).andReturn(securityContext).anyTimes();
        accessManager.delayChecks();
        expectLastCall().anyTimes();
        accessManager.checkNow();
        expectLastCall().anyTimes();
        expect(metamodel.getManagedTypes()).andReturn(new HashSet<ManagedType<?>>(Arrays.<ManagedType<?>>asList(
                abstractAclProtectedEntityType, aclProtectedEntityType, secondAclProtectedEntityType,
                abstractEntityType, groupType))).anyTimes();
        expect(metamodel.getEntities()).andReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(
                aclProtectedEntityType, secondAclProtectedEntityType, groupType))).anyTimes();
        expect(metamodel.managedType(AbstractAclProtectedEntity.class))
            .andReturn(abstractAclProtectedEntityType).anyTimes();
        expect(metamodel.managedType(AclProtectedEntity.class)).andReturn(aclProtectedEntityType).anyTimes();
        expect(metamodel.managedType(SecondAclProtectedEntity.class))
            .andReturn(secondAclProtectedEntityType).anyTimes();
        expect(metamodel.managedType(AbstractEntity.class)).andReturn(abstractEntityType).anyTimes();
        expect(metamodel.managedType(Group.class)).andReturn(groupType).anyTimes();
        expect(metamodel.entity(AbstractAclProtectedEntity.class))
            .andThrow(new IllegalArgumentException("not an entity")).anyTimes();
        expect(metamodel.entity(AclProtectedEntity.class)).andReturn(aclProtectedEntityType).anyTimes();
        expect(metamodel.entity(SecondAclProtectedEntity.class)).andReturn(secondAclProtectedEntityType).anyTimes();
        expect(metamodel.entity(AbstractEntity.class))
            .andThrow(new IllegalArgumentException("not an entity")).anyTimes();
        expect(metamodel.entity(Group.class)).andReturn(groupType).anyTimes();
        expect(abstractAclProtectedEntityType.getJavaType()).andReturn(AbstractAclProtectedEntity.class).anyTimes();
        expect(aclProtectedEntityType.getName()).andReturn(AclProtectedEntity.class.getSimpleName()).anyTimes();
        expect(aclProtectedEntityType.getJavaType()).andReturn(AclProtectedEntity.class).anyTimes();
        expect(secondAclProtectedEntityType.getName())
            .andReturn(SecondAclProtectedEntity.class.getSimpleName()).anyTimes();
        expect(secondAclProtectedEntityType.getJavaType()).andReturn(SecondAclProtectedEntity.class).anyTimes();
        expect(abstractEntityType.getJavaType()).andReturn(AbstractEntity.class).anyTimes();
        expect(groupType.getName()).andReturn(Group.class.getSimpleName()).anyTimes();
        expect(groupType.getJavaType()).andReturn(Group.class).anyTimes();

        replay(metamodel, accessManager, abstractAclProtectedEntityType, aclProtectedEntityType,
                secondAclProtectedEntityType, abstractEntityType, groupType, persistenceUnitUtil);
        AccessManager.Instance.register(accessManager);
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule(
                "GRANT ACCESS TO org.jpasecurity.model.acl.AccessControlled bean "
                    + "WHERE (bean.accessControlList is null) OR (bean.accessControlList=CURRENT_PRINCIPAL))"
            );
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);
        accessRules = compiler.compile(rule);
        filter = new EntityFilter(metamodel, persistenceUnitUtil, accessRules);
    }

    @After
    public void removeAccessManager() {
        AccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void checkStatementForConcreteSubtype() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), SecondAclProtectedEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ, Collections.<Alias>emptySet());
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals("(((i.accessControlList IS  NULL ) OR (i.accessControlList = :CURRENT_PRINCIPAL)))",
            accessRules1.toString());
    }

    @Test
    public void checkStatementForProtectedBaseSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractAclProtectedEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ, Collections.<Alias>emptySet());
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
            filter.createAccessDefinition(selectedTypes, AccessType.READ, Collections.<Alias>emptySet());
        final Node accessRules1 = accessDefinition.getAccessRules();
        Assert.assertEquals("(((i.accessControlList IS  NULL ) OR (i.accessControlList = :CURRENT_PRINCIPAL)) "
                + "AND ((b.accessControlList IS  NULL ) OR (b.accessControlList = :CURRENT_PRINCIPAL)))",
            accessRules1.toString());
    }

    @Ignore("TODO: Find a better way to assert result")
    @Test
    public void checkStatementForNotProtectedSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ, Collections.<Alias>emptySet());
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

    @Ignore("TODO: Find a better way to assert result")
    @Test
    public void checkStatementForMultipleNotProtectedSuperType() throws Exception {
        final HashMap<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        selectedTypes.put(new Path("i"), AbstractEntity.class);
        selectedTypes.put(new Path("b"), AbstractEntity.class);
        final EntityFilter.AccessDefinition accessDefinition =
            filter.createAccessDefinition(selectedTypes, AccessType.READ, Collections.<Alias>emptySet());
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
