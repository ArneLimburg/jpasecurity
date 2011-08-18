/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.entity;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureSortedSetTest extends AbstractSecureCollectionTestCase<SecureSortedSet<Object>> {

    private List<Object> testDataWithComparator;
    private List<ComparableObject> testDataWithoutComparator;
    private SortedSet<Object> originalWithComparator;
    private SortedSet<ComparableObject> originalWithoutComparator;
    private SecureSortedSet<Object> secureWithComparator;
    private SecureSortedSet<ComparableObject> secureWithoutComparator;
    private AccessManager accessManager;

    @Before
    public void createTestData() {
        super.createTestData();
        accessManager = createMock(AccessManager.class);
        testDataWithoutComparator = Arrays.asList(new ComparableObject(),
                                                  new ComparableObject(),
                                                  new ComparableObject(),
                                                  new ComparableObject());
        testDataWithComparator = Arrays.asList(new Object(), new Object(), new Object(), new Object());
        Collections.sort(testDataWithoutComparator);
        Collections.sort(testDataWithComparator, new HashCodeComparator());
        originalWithoutComparator = new TreeSet<ComparableObject>(testDataWithoutComparator);
        originalWithComparator = new TreeSet<Object>(new HashCodeComparator());
        originalWithComparator.addAll(testDataWithComparator);
        secureWithComparator = new SecureSortedSet<Object>(originalWithComparator, getObjectManager(), accessManager);
        secureWithoutComparator
            = new SecureSortedSet<ComparableObject>(originalWithoutComparator, getObjectManager(), accessManager);
    }

    @Test
    public void comparator() {
        expect(accessManager.isAccessible((AccessType)anyObject(), anyObject())).andReturn(true).anyTimes();
        replay(accessManager);
        assertEquals(originalWithComparator.comparator(), secureWithComparator.comparator());
        assertNull(secureWithoutComparator.comparator());
    }

    @Test
    public void first() {
        expectAccessibleWithoutComparator(true, true, true, true);
        replay(accessManager);
        assertTrue(secureWithoutComparator.first().equals(testDataWithoutComparator.get(0)));

        reset(accessManager);

        expectAccessibleWithComparator(false, true, true, true);
        replay(accessManager);
        assertTrue(secureWithComparator.first().equals(testDataWithComparator.get(1)));
    }

    @Test
    public void last() {
        expectAccessibleWithoutComparator(true, true, true, true);
        replay(accessManager);
        assertTrue(secureWithoutComparator.last().equals(testDataWithoutComparator.get(3)));

        reset(accessManager);

        expectAccessibleWithComparator(true, true, true, false);
        replay(accessManager);
        assertTrue(secureWithComparator.last().equals(testDataWithComparator.get(2)));
    }

    @Test
    public void headSet() {
        expectAccessibleWithoutComparator(true, true, true, true);
        replay(accessManager);
        SortedSet<ComparableObject> headSetWithoutComparator
            = secureWithoutComparator.headSet(testDataWithoutComparator.get(3));
        assertEquals(3, headSetWithoutComparator.size());
        assertTrue(headSetWithoutComparator.first().equals(testDataWithoutComparator.get(0)));
        assertTrue(headSetWithoutComparator.contains(testDataWithoutComparator.get(1)));
        assertTrue(headSetWithoutComparator.last().equals(testDataWithoutComparator.get(2)));

        reset(accessManager);

        expectAccessibleWithComparator(false, true, true, true);
        replay(accessManager);
        SortedSet<Object> headSetWithComparator = secureWithComparator.headSet(testDataWithComparator.get(3));
        assertEquals(2, headSetWithComparator.size());
        assertTrue(headSetWithComparator.first().equals(testDataWithComparator.get(1)));
        assertTrue(headSetWithComparator.last().equals(testDataWithComparator.get(2)));
    }

    @Test
    public void subSet() {
        expectAccessibleWithoutComparator(true, true, false, true);
        replay(accessManager);
        SortedSet<ComparableObject> subSetWithoutComparator
            = secureWithoutComparator.subSet(testDataWithoutComparator.get(1), testDataWithoutComparator.get(3));
        assertEquals(1, subSetWithoutComparator.size());
        assertTrue(subSetWithoutComparator.first().equals(testDataWithoutComparator.get(1)));
        assertTrue(subSetWithoutComparator.last().equals(testDataWithoutComparator.get(1)));

        reset(accessManager);

        expectAccessibleWithComparator(false, true, true, true);
        replay(accessManager);
        SortedSet<Object> subSetWithComparator
            = secureWithComparator.subSet(testDataWithComparator.get(1), testDataWithComparator.get(3));
        assertEquals(2, subSetWithComparator.size());
        assertTrue(subSetWithComparator.first().equals(testDataWithComparator.get(1)));
        assertTrue(subSetWithComparator.last().equals(testDataWithComparator.get(2)));
    }

    @Test
    public void tailSet() {
        expectAccessibleWithoutComparator(true, true, false, true);
        replay(accessManager);
        SortedSet<ComparableObject> tailSetWithoutComparator
            = secureWithoutComparator.tailSet(testDataWithoutComparator.get(1));
        assertEquals(2, tailSetWithoutComparator.size());
        assertTrue(tailSetWithoutComparator.first().equals(testDataWithoutComparator.get(1)));
        assertTrue(tailSetWithoutComparator.last().equals(testDataWithoutComparator.get(3)));

        reset(accessManager);

        expectAccessibleWithComparator(false, true, true, false);
        replay(accessManager);
        SortedSet<Object> tailSetWithComparator = secureWithComparator.tailSet(testDataWithComparator.get(1));
        assertEquals(2, tailSetWithComparator.size());
        assertTrue(tailSetWithComparator.first().equals(testDataWithComparator.get(1)));
        assertTrue(tailSetWithComparator.last().equals(testDataWithComparator.get(2)));
    }

    public SecureSortedSet<Object> createSecureCollection(AbstractSecureObjectManager objectManager,
                                                          SecureEntity... secureEntities) {
        SortedSet<Object> original = new TreeSet<Object>(new HashCodeComparator());
        SortedSet<Object> filtered = new TreeSet<Object>(new HashCodeComparator());
        for (SecureEntity secureEntity: secureEntities) {
            original.add(objectManager.getUnsecureObject(secureEntity));
            filtered.add(secureEntity);
        }
        return new SecureSortedSet<Object>(original, filtered, objectManager);
    }

    private void expectAccessibleWithComparator(boolean object1, boolean object2, boolean object3, boolean object4) {
        expect(accessManager.isAccessible(AccessType.READ, testDataWithComparator.get(0))).andReturn(object1);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithComparator.get(1))).andReturn(object2);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithComparator.get(2))).andReturn(object3);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithComparator.get(3))).andReturn(object4);
    }

    private void expectAccessibleWithoutComparator(boolean object1, boolean object2, boolean object3, boolean object4) {
        expect(accessManager.isAccessible(AccessType.READ, testDataWithoutComparator.get(0))).andReturn(object1);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithoutComparator.get(1))).andReturn(object2);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithoutComparator.get(2))).andReturn(object3);
        expect(accessManager.isAccessible(AccessType.READ, testDataWithoutComparator.get(3))).andReturn(object4);
    }

    private static class HashCodeComparator implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            return o1.hashCode() - o2.hashCode();
        }
    }

    public static class ComparableObject implements Comparable<Object> {

        public int compareTo(Object o) {
            return hashCode() - o.hashCode();
        }

        public boolean equals(Object object) {
            return compareTo(object) == 0;
        }

        public int hashCode() {
            return super.hashCode();
        }
    }
}
