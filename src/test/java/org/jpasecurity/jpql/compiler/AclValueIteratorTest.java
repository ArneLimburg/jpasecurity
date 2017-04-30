/*
 * Copyright 2011 - 2016 Stefan Hildebrandt, Arne Limburg
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
package org.jpasecurity.jpql.compiler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.model.acl.Acl;
import org.jpasecurity.model.acl.AclEntry;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.User;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Stefan Hildebrandt
 */
public class AclValueIteratorTest {

    private static final Alias ACL = new Alias("acl");
    private static final Alias ACL_ENTRY = new Alias("entry");
    private static final Alias USER = new Alias("user");
    private static final Alias GROUP = new Alias("groups");
    private static final Alias GROUP_FULL_HIERARCHY = new Alias("fullHierarchy");

    private Set<TypeDefinition> typeDefinitions;
    private SetMap<Alias, Object> possibleValues;

    @Before
    public void createTestData() {
        typeDefinitions = new HashSet<TypeDefinition>();
        typeDefinitions.add(new TypeDefinition(GROUP, Group.class, new Path("user.groups"), true));
        typeDefinitions.add(new TypeDefinition(ACL_ENTRY, AclEntry.class));
        typeDefinitions.add(new TypeDefinition(GROUP_FULL_HIERARCHY,
                                               Group.class,
                                               new Path("groups.fullHierarchy"),
                                               true));
        typeDefinitions.add(new TypeDefinition(USER, User.class));
        typeDefinitions.add(new TypeDefinition(ACL, Acl.class));
        possibleValues = new SetHashMap<Alias, Object>();
    }

    @Test(expected = NoSuchElementException.class)
    public void acl() throws NoSuchFieldException {
        int groupId = 0;
        final Group group1 = new Group();
        group1.setId(++groupId);
        group1.setFullHierarchy(Arrays.asList(group1));
        possibleValues.add(GROUP, group1);
        possibleValues.add(GROUP_FULL_HIERARCHY, group1);
        final Group group2 = new Group();
        group2.setId(++groupId);
        group2.setFullHierarchy(Arrays.asList(group2));
        possibleValues.add(GROUP, group2);
        possibleValues.add(GROUP_FULL_HIERARCHY, group2);
        final Group group3 = new Group();
        group3.setId(++groupId);
        group3.setFullHierarchy(Arrays.asList(group3));
        possibleValues.add(GROUP, group3);
        possibleValues.add(GROUP_FULL_HIERARCHY, group3);
        final Group group4 = new Group();
        group4.setId(++groupId);
        group4.setFullHierarchy(Arrays.asList(group4));
        possibleValues.add(GROUP, group4);
        possibleValues.add(GROUP_FULL_HIERARCHY, group4);
        final Group group5 = new Group();
        group5.setId(++groupId);
        group5.setFullHierarchy(Arrays.asList(group5));
        possibleValues.add(GROUP, group5);
        possibleValues.add(GROUP_FULL_HIERARCHY, group5);
        final Group group6 = new Group();
        group6.setId(++groupId);
        group6.setFullHierarchy(Arrays.asList(group6));
        possibleValues.add(GROUP, group6);
        possibleValues.add(GROUP_FULL_HIERARCHY, group6);
        final Group group7 = new Group();
        group7.setId(++groupId);
        group7.setFullHierarchy(Arrays.asList(group7));
        possibleValues.add(GROUP, group7);
        possibleValues.add(GROUP_FULL_HIERARCHY, group7);
        final Group group8 = new Group();
        group8.setId(++groupId);
        group8.setFullHierarchy(Arrays.asList(group8));
        possibleValues.add(GROUP, group8);
        possibleValues.add(GROUP_FULL_HIERARCHY, group8);
        final Group group9 = new Group();
        group9.setId(++groupId);
        group9.setFullHierarchy(Arrays.asList(group9));
        possibleValues.add(GROUP, group9);
        possibleValues.add(GROUP_FULL_HIERARCHY, group9);
        final Group group10 = new Group();
        group10.setId(++groupId);
        group10.setFullHierarchy(Arrays.asList(group10));
        possibleValues.add(GROUP, group10);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10);
        final AclEntry aclEntry = new AclEntry();
        aclEntry.setId(1);
        aclEntry.setGroup(group10);
        possibleValues.add(ACL_ENTRY, aclEntry);
        final Acl acl = new Acl();
        acl.setId(1);
        acl.setEntries(Arrays.asList(aclEntry));
        possibleValues.add(ACL, acl);
        User user = new User();
        user.setId(1);
        user.setGroups(Arrays.asList(group10));
        possibleValues.add(USER, user);
        PathEvaluator pathEvaluator = createPathEvaluator();
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertTrue(valueIterator.hasNext());
        assertNotNull(valueIterator.next());
        assertFalse(valueIterator.hasNext());
        valueIterator.next();
    }

    private MappedPathEvaluator createPathEvaluator() throws NoSuchFieldException {
        Metamodel metamodel = mock(Metamodel.class);
        SecurePersistenceUnitUtil persistenceUnitUtil = mock(SecurePersistenceUnitUtil.class);
        EntityType userType = mock(EntityType.class);
        EntityType groupType = mock(EntityType.class);
        Attribute groupsAttribute = mock(Attribute.class);
        Attribute fullHierarchyAttribute = mock(Attribute.class);
        when(metamodel.managedType(User.class)).thenReturn(userType);
        when(metamodel.managedType(Group.class)).thenReturn(groupType);
        when(persistenceUnitUtil.isLoaded(any())).thenReturn(true);
        when(persistenceUnitUtil.initialize(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        });
        when(userType.getAttributes()).thenReturn(Collections.singleton(groupsAttribute));
        when(userType.getAttribute("groups")).thenReturn(groupsAttribute);
        when(groupType.getAttributes()).thenReturn(Collections.singleton(fullHierarchyAttribute));
        when(groupType.getAttribute("fullHierarchy")).thenReturn(fullHierarchyAttribute);
        when(groupsAttribute.getName()).thenReturn("groups");
        when(groupsAttribute.getJavaMember()).thenReturn(User.class.getDeclaredField("groups"));
        when(fullHierarchyAttribute.getName()).thenReturn("fullHierarchy");
        when(fullHierarchyAttribute.getJavaMember())
            .thenReturn(Group.class.getDeclaredField("fullHierarchy"));
        return new MappedPathEvaluator(metamodel, persistenceUnitUtil);
    }
}
