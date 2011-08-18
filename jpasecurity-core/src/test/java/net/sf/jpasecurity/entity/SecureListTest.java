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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureListTest extends AbstractSecureCollectionTestCase<SecureList<Object>> {

    @Test
    public void set() {
        AbstractSecureObjectManager objectManager = getObjectManager();
        SecureList<Object> secureList = createSecureCollection(objectManager);
        List<Object> unsecureList = objectManager.getUnsecureObject(secureList);
        secureList.add(null);
        flush(secureList);
        assertEquals(1, secureList.size());
        assertEquals(1, unsecureList.size());
        assertNull(secureList.iterator().next());
        assertNull(unsecureList.iterator().next());

        secureList.set(0, getSecureEntity());

        assertEquals(1, secureList.size());
        assertEquals(1, unsecureList.size());
        assertEquals(getSecureEntity(), secureList.iterator().next());
        assertNull(unsecureList.iterator().next());

        flush(secureList);

        assertEquals(1, secureList.size());
        assertEquals(1, unsecureList.size());
        assertEquals(getSecureEntity(), secureList.iterator().next());
        assertEquals(getUnsecureEntity(), unsecureList.iterator().next());
    }

    @Test
    public void add() {
        super.add();
        final AbstractSecureObjectManager objectManager = getObjectManager();
        final SecureList<Object> secureList = createSecureCollection(objectManager);
        final List<Object> unsecureList = objectManager.getUnsecureObject(secureList);
        assertEquals(objectManager, secureList.getObjectManager());
        add(secureList, unsecureList, new Runnable() {
            public void run() {
                secureList.add(0, getSecureEntity());
            }
        });

        secureList.add(0, null);
        assertEquals(2, secureList.size());
        assertEquals(1, unsecureList.size());
        assertNull(secureList.iterator().next());
        assertEquals(getUnsecureEntity(), unsecureList.iterator().next());

        flush(secureList);

        assertEquals(2, secureList.size());
        assertEquals(2, unsecureList.size());
        assertNull(secureList.iterator().next());
        assertNull(unsecureList.iterator().next());
    }

    @Test
    public void addAll() {
        super.addAll();
        final AbstractSecureObjectManager objectManager = getObjectManager();
        final SecureList<Object> secureList = createSecureCollection(objectManager);
        final List<Object> unsecureList = objectManager.getUnsecureObject(secureList);
        assertEquals(objectManager, secureList.getObjectManager());
        add(secureList, unsecureList, new Runnable() {
            public void run() {
                secureList.addAll(0, Collections.singletonList(getSecureEntity()));
            }
        });

        secureList.addAll(0, Collections.singletonList(null));
        assertEquals(2, secureList.size());
        assertEquals(1, unsecureList.size());
        assertNull(secureList.iterator().next());
        assertEquals(getUnsecureEntity(), unsecureList.iterator().next());

        flush(secureList);

        assertEquals(2, secureList.size());
        assertEquals(2, unsecureList.size());
        assertNull(secureList.iterator().next());
        assertNull(unsecureList.iterator().next());
    }

    public SecureList<Object> createSecureCollection(AbstractSecureObjectManager objectManager,
                                                     SecureEntity... secureEntities) {
        List<Object> original = new ArrayList<Object>();
        List<Object> filtered = new ArrayList<Object>();
        for (SecureEntity secureEntity: secureEntities) {
            original.add(objectManager.getUnsecureObject(secureEntity));
            filtered.add(secureEntity);
        }
        return new SecureList<Object>(original, filtered, objectManager);
    }

    public void flush(SecureCollection<Object> secureCollection) {
        ((SecureList<Object>)secureCollection).flush();
    }
}
