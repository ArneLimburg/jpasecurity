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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
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
    private DefaultAccessManager accessManager;
    private Collection<AccessRule> accessRules;
    private EntityFilter filter;

    @Before
    public void initialize() throws Exception {
        metamodel = mock(Metamodel.class);
        MappedSuperclassType abstractAclProtectedEntityType = mock(MappedSuperclassType.class);
        EntityType aclProtectedEntityType = mock(EntityType.class);
        EntityType secondAclProtectedEntityType = mock(EntityType.class);
        MappedSuperclassType abstractEntityType = mock(MappedSuperclassType.class);
        EntityType groupType = mock(EntityType.class);
        SecurePersistenceUnitUtil persistenceUnitUtil = mock(SecurePersistenceUnitUtil.class);
        accessManager = mock(DefaultAccessManager.class);
        DefaultSecurityContext securityContext = new DefaultSecurityContext();
        securityContext.register(new Alias("CURRENT_PRINCIPAL"), "user");
        when(accessManager.getContext()).thenReturn(securityContext);
        when(metamodel.getManagedTypes()).thenReturn(new HashSet<ManagedType<?>>(Arrays.<ManagedType<?>>asList(
                abstractAclProtectedEntityType, aclProtectedEntityType, secondAclProtectedEntityType,
                abstractEntityType, groupType)));
        when(metamodel.getEntities()).thenReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(
                aclProtectedEntityType, secondAclProtectedEntityType, groupType)));
        when(metamodel.managedType(AbstractAclProtectedEntity.class))
            .thenReturn(abstractAclProtectedEntityType);
        when(metamodel.managedType(AclProtectedEntity.class)).thenReturn(aclProtectedEntityType);
        when(metamodel.managedType(SecondAclProtectedEntity.class))
            .thenReturn(secondAclProtectedEntityType);
        when(metamodel.managedType(AbstractEntity.class)).thenReturn(abstractEntityType);
        when(metamodel.managedType(Group.class)).thenReturn(groupType);
        when(metamodel.entity(AbstractAclProtectedEntity.class))
            .thenThrow(new IllegalArgumentException("not an entity"));
        when(metamodel.entity(AclProtectedEntity.class)).thenReturn(aclProtectedEntityType);
        when(metamodel.entity(SecondAclProtectedEntity.class)).thenReturn(secondAclProtectedEntityType);
        when(metamodel.entity(AbstractEntity.class))
            .thenThrow(new IllegalArgumentException("not an entity"));
        when(metamodel.entity(Group.class)).thenReturn(groupType);
        when(abstractAclProtectedEntityType.getJavaType()).thenReturn(AbstractAclProtectedEntity.class);
        when(aclProtectedEntityType.getName()).thenReturn(AclProtectedEntity.class.getSimpleName());
        when(aclProtectedEntityType.getJavaType()).thenReturn(AclProtectedEntity.class);
        when(secondAclProtectedEntityType.getName())
            .thenReturn(SecondAclProtectedEntity.class.getSimpleName());
        when(secondAclProtectedEntityType.getJavaType()).thenReturn(SecondAclProtectedEntity.class);
        when(abstractEntityType.getJavaType()).thenReturn(AbstractEntity.class);
        when(groupType.getName()).thenReturn(Group.class.getSimpleName());
        when(groupType.getJavaType()).thenReturn(Group.class);

        DefaultAccessManager.Instance.register(accessManager);
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
        DefaultAccessManager.Instance.unregister(accessManager);
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
