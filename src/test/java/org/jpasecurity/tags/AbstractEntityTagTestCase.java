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
package org.jpasecurity.tags;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import junit.framework.TestCase;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;

/**
 * @author Arne Limburg
 */
public abstract class AbstractEntityTagTestCase extends TestCase {

    private PageContext pageContext = new MockPageContext();
    private Object entity = new Object();
    private AbstractEntityTag entityTag;
    private AccessManager accessManager;

    public void setUp() {
        entityTag = createEntityTag();
        entityTag.setPageContext(pageContext);
        entityTag.setEntity("testEntity");
        accessManager = mock(AccessManager.class);
    }

    public void tearDown() {
        entityTag.release();
    }

    public void testAccessiblePageScope() {
        initializeEntity(PageContext.PAGE_SCOPE);
        initializeAccessManager(PageContext.PAGE_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testNotAccessiblePageScope() {
        initializeEntity(PageContext.PAGE_SCOPE);
        initializeAccessManager(PageContext.PAGE_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testAccessibleRequestScope() {
        initializeEntity(PageContext.REQUEST_SCOPE);
        initializeAccessManager(PageContext.REQUEST_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testNotAccessibleRequestScope() {
        initializeEntity(PageContext.REQUEST_SCOPE);
        initializeAccessManager(PageContext.REQUEST_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testAccessibleSessionScope() {
        initializeEntity(PageContext.SESSION_SCOPE);
        initializeAccessManager(PageContext.SESSION_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testNotAccessibleSessionScope() {
        initializeEntity(PageContext.SESSION_SCOPE);
        initializeAccessManager(PageContext.SESSION_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testAccessibleApplicationScope() {
        initializeEntity(PageContext.APPLICATION_SCOPE);
        initializeAccessManager(PageContext.APPLICATION_SCOPE, true);
        assertEquals(Tag.EVAL_BODY_INCLUDE, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testNotAccessibleApplicationScope() {
        initializeEntity(PageContext.APPLICATION_SCOPE);
        initializeAccessManager(PageContext.APPLICATION_SCOPE, false);
        assertEquals(Tag.SKIP_BODY, entityTag.doStartTag());
        verify(accessManager).isAccessible(getAccessType(), entity);
    }

    public void testNoEntity() {
        initializeAccessManager(PageContext.PAGE_SCOPE, true);
        try {
            entityTag.doStartTag();
            fail();
        } catch (IllegalStateException e) {
            // expected, since no entity is present
        }
    }

    public void testNoAccessManager() {
        initializeEntity(PageContext.PAGE_SCOPE);
        try {
            entityTag.doStartTag();
            fail();
        } catch (IllegalStateException e) {
            // expected, since no access manager is present
        }
    }

    public void initializeEntity(int scope) {
        initializeEntity(entity, scope);
    }

    public void initializeEntity(Object entity, int scope) {
        pageContext.setAttribute(entityTag.getEntity(), entity, scope);
    }

    public void initializeAccessManager(int scope, boolean accessible) {
        initializeAccessManager(accessManager, scope, accessible);
    }

    public void initializeAccessManager(AccessManager accessManager, int scope, boolean accessible) {
        if (accessManager != null) {
            pageContext.setAttribute("accessManager", accessManager, scope);
            when(accessManager.isAccessible(getAccessType(), entity)).thenReturn(accessible);
        }
    }

    public abstract AbstractEntityTag createEntityTag();

    public abstract AccessType getAccessType();
}
