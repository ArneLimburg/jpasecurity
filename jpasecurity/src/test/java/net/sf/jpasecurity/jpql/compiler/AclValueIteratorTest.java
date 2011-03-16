/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.spi.PersistenceUnitInfo;

import org.easymock.IAnswer;

import junit.framework.TestCase;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.acl.Acl;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.User;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import net.sf.jpasecurity.util.SetHashMap;
import net.sf.jpasecurity.util.SetMap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/** @author Stefan Hildebrandt */
public class AclValueIteratorTest extends TestCase {

    private static final Alias ACL = new Alias("acl");
    private static final Alias ACL_ENTRY = new Alias("entry");
    private static final Alias USER = new Alias("user");
    private static final Alias GROUP = new Alias("groups");
    private static final Alias GROUP_FULL_HIERARCHY = new Alias("fullHierarchy");
    private static final int MAX_COUNT = 1000000;

    private Set<TypeDefinition> typeDefinitions;
    private SetMap<Alias, Object> possibleValues;

    public void setUp() {
        typeDefinitions = new HashSet<TypeDefinition>();
        typeDefinitions.add(new TypeDefinition(GROUP, Group.class, "user3.groups", true));
        typeDefinitions.add(new TypeDefinition(ACL_ENTRY, AclEntry.class));
        typeDefinitions.add(new TypeDefinition(GROUP_FULL_HIERARCHY, Group.class, "groups3.fullHierarchy", true));
        typeDefinitions.add(new TypeDefinition(USER, User.class));
        typeDefinitions.add(new TypeDefinition(ACL, Acl.class));
        possibleValues = new SetHashMap<Alias, Object>();
    }

    public void testNextWithFourPossibleValues() {
        final Group group10004623 = new Group();
        group10004623.setId(10004623);
        group10004623.setFullHierarchy(Arrays.asList(group10004623));
        possibleValues.add(GROUP, group10004623);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004623);
        final Group group10004700 = new Group();
        group10004700.setId(10004700);
        group10004700.setFullHierarchy(Arrays.asList(group10004700));
        possibleValues.add(GROUP, group10004700);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004700);
        final Group group10004633 = new Group();
        group10004633.setId(10004633);
        group10004633.setFullHierarchy(Arrays.asList(group10004633));
        possibleValues.add(GROUP, group10004633);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004633);
        final Group group10004631 = new Group();
        group10004631.setId(10004631);
        group10004631.setFullHierarchy(Arrays.asList(group10004631));
        possibleValues.add(GROUP, group10004631);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004631);
        final Group group10004629 = new Group();
        group10004629.setId(10004629);
        group10004629.setFullHierarchy(Arrays.asList(group10004629));
        possibleValues.add(GROUP, group10004629);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004629);
        final Group group10004630 = new Group();
        group10004630.setId(10004630);
        group10004630.setFullHierarchy(Arrays.asList(group10004630));
        possibleValues.add(GROUP, group10004630);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004630);
        final Group group10004635 = new Group();
        group10004635.setId(10004635);
        group10004635.setFullHierarchy(Arrays.asList(group10004635));
        possibleValues.add(GROUP, group10004635);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004635);
        final Group group10004632 = new Group();
        group10004632.setId(10004632);
        group10004632.setFullHierarchy(Arrays.asList(group10004632));
        possibleValues.add(GROUP, group10004632);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004632);
        final Group group10004634 = new Group();
        group10004634.setId(10004634);
        group10004634.setFullHierarchy(Arrays.asList(group10004634));
        possibleValues.add(GROUP, group10004634);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004634);
        final Group group10004636 = new Group();
        group10004636.setId(10004636);
        group10004636.setFullHierarchy(Arrays.asList(group10004636));
        possibleValues.add(GROUP, group10004636);
        possibleValues.add(GROUP_FULL_HIERARCHY, group10004636);
        final AclEntry aclEntry = new AclEntry();
        aclEntry.setId(1);
        aclEntry.setGroup(group10004636);
        possibleValues.add(ACL_ENTRY, aclEntry);
        final Acl acl = new Acl();
        acl.setId(1);
        acl.setTrademarkId(1L);
        acl.setEntries(Arrays.asList(aclEntry));
        possibleValues.add(ACL, acl);
        User user = new User();
        user.setId(1);
        user.setGroups(Arrays.asList(group10004636));
        possibleValues.add(USER, user);
        PathEvaluator pathEvaluator = createPathEvaluator();
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        int count = 0;
        while (valueIterator.hasNext() && count < MAX_COUNT) {
            valueIterator.next();
            count++;
        }
        assertEquals(100,count);
    }

    private MappedPathEvaluator createPathEvaluator() {
        PersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(Group.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(Acl.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(AclEntry.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(User.class.getName());
        final MappingInformation mappingInformation = new JpaAnnotationParser().parse(persistenceUnitInfo);
        return new MappedPathEvaluator(mappingInformation);
    }

    private void assertNext(Map<Alias, Object> values, Iterator<Map<Alias, Object>> i) {
        assertTrue(i.hasNext());
        Map<Alias, Object> result = i.next();
        for (Map.Entry<Alias, Object> entry : values.entrySet()) {
            assertEquals(entry.getValue(), result.get(entry.getKey()));
        }
    }

    private void assertNoSuchElementException(Iterator<?> i) {
        assertFalse(i.hasNext());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException e) {
            //expected
        }
    }

    private class PathEvaluatorFactory {

        private FieldAccessAnnotationTestBean[] beans;

        public PathEvaluatorFactory withBeans(FieldAccessAnnotationTestBean... beans) {
            this.beans = beans;
            return this;
        }

        public PathEvaluatorFactory withNoBeans() {
            return withBeans();
        }

        public PathEvaluatorFactory withBean(FieldAccessAnnotationTestBean bean) {
            return withBeans(bean);
        }

        public PathEvaluator create() {
            PathEvaluator pathEvaluator = createMock(PathEvaluator.class);
            for (FieldAccessAnnotationTestBean bean : beans) {
                expect(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("parent")))
                    .andAnswer(new ParentAnswer(bean)).anyTimes();
                expect(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("children")))
                    .andAnswer(new ChildrenAnswer(bean)).anyTimes();
            }
            replay(pathEvaluator);
            return pathEvaluator;
        }
    }

    private class ParentAnswer implements IAnswer<List<Object>> {

        private FieldAccessAnnotationTestBean bean;

        public ParentAnswer(FieldAccessAnnotationTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer() throws Throwable {
            return bean.getParentBean() == null ?
                Collections.emptyList() :
                Collections.<Object>singletonList(bean.getParentBean());
        }
    }

    private class ChildrenAnswer implements IAnswer<List<Object>> {

        private FieldAccessAnnotationTestBean bean;

        public ChildrenAnswer(FieldAccessAnnotationTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer() throws Throwable {
            return bean.getChildBeans() == null || bean.getChildBeans().isEmpty() ?
                Collections.emptyList() :
                new ArrayList<Object>(bean.getChildBeans());
        }
    }
}
