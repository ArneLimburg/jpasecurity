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

import static net.sf.jpasecurity.AccessType.READ;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureSetTest extends AbstractSecureCollectionTestCase {

    public SecureCollection<Object> createSecureCollection(AbstractSecureObjectManager objectManager,
                                                           SecureEntity... secureEntities) {
        Set<Object> original = new HashSet<Object>();
        Set<Object> filtered = new HashSet<Object>();
        for (SecureEntity secureEntity: secureEntities) {
            original.add(objectManager.getUnsecureObject(secureEntity));
            filtered.add(secureEntity);
        }
        return new SecureSet<Object>(original, filtered, objectManager);
    }

    @Test
    public void filter() {

        Object object1 = new Object();
        Object object2 = new Object();
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(READ, object1)).andReturn(true);
        expect(accessManager.isAccessible(READ, object2)).andReturn(false);
        replay(accessManager);

        Set<Object> original = new HashSet<Object>();
        original.add(object1);
        original.add(object2);

        SecureSet<Object> filtered = new SecureSet<Object>(original, getObjectManager(), accessManager);
        assertEquals(1, filtered.size());
        assertTrue(filtered.iterator().next().equals(object1));
        assertFalse(filtered.contains(object2));
    }
}
