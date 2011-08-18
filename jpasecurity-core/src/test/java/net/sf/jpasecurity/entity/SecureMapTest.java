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
package net.sf.jpasecurity.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureMapTest extends AbstractSecureObjectTestCase {

    public static final String MAP_KEY = "key";
    private Map<String, Object> unsecureMap;
    private Map<String, Object> filteredMap;
    private DefaultSecureMap<String, Object> secureMap;

    @Before
    public void initialize() {
        filteredMap = new HashMap<String, Object>();
        unsecureMap = new HashMap<String, Object>();
        secureMap = new DefaultSecureMap<String, Object>(unsecureMap, filteredMap, getObjectManager());
    }

    @Test
    public void put() {
        put(new Runnable() {
            public void run() {
                secureMap.put(MAP_KEY, getSecureEntity());
            }
        });
    }

    @Test
    public void remove() {
        remove(new Runnable() {
            public void run() {
                assertEquals(getSecureEntity(), secureMap.remove(MAP_KEY));
            }
        });
    }

    @Test
    public void putAll() {
        put(new Runnable() {
            public void run() {
                secureMap.putAll(Collections.singletonMap(MAP_KEY, getSecureEntity()));
            }
        });
    }

    @Test
    public void clear() {
        remove(new Runnable() {
            public void run() {
                secureMap.clear();
            }
        });
    }

    protected void put(Runnable putOperation) {
        assertEquals(0, secureMap.size());

        putOperation.run();

        assertEquals(1, secureMap.size());
        assertEquals(0, unsecureMap.size());
        assertEquals(getSecureEntity(), secureMap.get(MAP_KEY));

        secureMap.flush();

        assertEquals(1, secureMap.size());
        assertEquals(1, unsecureMap.size());
        assertEquals(getSecureEntity(), secureMap.get(MAP_KEY));
        assertEquals(getUnsecureEntity(), unsecureMap.get(MAP_KEY));
    }

    protected void remove(Runnable removeOperation) {
        unsecureMap.put(MAP_KEY, getUnsecureEntity());
        filteredMap.put(MAP_KEY, getSecureEntity());
        assertEquals(1, secureMap.size());
        assertEquals(1, unsecureMap.size());

        removeOperation.run();

        assertEquals(0, secureMap.size());
        assertEquals(1, unsecureMap.size());
        assertFalse(secureMap.values().iterator().hasNext());
        assertEquals(getUnsecureEntity(), unsecureMap.values().iterator().next());

        secureMap.flush();

        assertEquals(0, secureMap.size());
        assertEquals(0, unsecureMap.size());
        assertFalse(secureMap.values().iterator().hasNext());
        assertFalse(unsecureMap.values().iterator().hasNext());
    }
}
