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
package net.sf.jpasecurity.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessUtilsTest {

    private Object entity = new Object();

    @Test
    public void canCreate() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.CREATE, entity)).andReturn(true);
        replay(accessManager);
        assertTrue(AccessUtils.canCreate(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void mayNotCreate() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.CREATE, entity)).andReturn(false);
        replay(accessManager);
        assertFalse(AccessUtils.canCreate(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void canRead() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.READ, entity)).andReturn(true);
        replay(accessManager);
        assertTrue(AccessUtils.canRead(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void mayNotRead() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.READ, entity)).andReturn(false);
        replay(accessManager);
        assertFalse(AccessUtils.canRead(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void canUpdate() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.UPDATE, entity)).andReturn(true);
        replay(accessManager);
        assertTrue(AccessUtils.canUpdate(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void mayNotUpdate() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.UPDATE, entity)).andReturn(false);
        replay(accessManager);
        assertFalse(AccessUtils.canUpdate(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void canDelete() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.DELETE, entity)).andReturn(true);
        replay(accessManager);
        assertTrue(AccessUtils.canDelete(accessManager, entity));
        verify(accessManager);
    }

    @Test
    public void mayNotDelete() {
        AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.DELETE, entity)).andReturn(false);
        replay(accessManager);
        assertFalse(AccessUtils.canDelete(accessManager, entity));
        verify(accessManager);
    }
}
