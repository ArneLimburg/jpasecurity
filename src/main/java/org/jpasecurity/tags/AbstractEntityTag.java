/*
 * Copyright 2009 Arne Limburg
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

import javax.servlet.jsp.PageContext;

import org.jpasecurity.AccessType;

/**
 * A tag that renders the jsp body only if a specified entity is accessible.
 * @author Arne Limburg
 */
public abstract class AbstractEntityTag extends AbstractSecurityTag {

    private String entityName;

    public String getEntity() {
        return entityName;
    }

    public void setEntity(String entity) {
        this.entityName = entity;
    }

    public void release() {
        entityName = null;
        super.release();
    }

    protected boolean isAccessible() {
        Object entity = resolveEntity();
        return getAccessManager().isAccessible(getAccessType(), entity);
    }

    protected abstract AccessType getAccessType();

    private Object resolveEntity() {

        Object entity;

        entity = pageContext.getAttribute(entityName, PageContext.PAGE_SCOPE);
        if (entity != null) {
            return entity;
        }
        entity = pageContext.getAttribute(entityName, PageContext.REQUEST_SCOPE);
        if (entity != null) {
            return entity;
        }
        entity = pageContext.getAttribute(entityName, PageContext.SESSION_SCOPE);
        if (entity != null) {
            return entity;
        }
        entity = pageContext.getAttribute(entityName, PageContext.APPLICATION_SCOPE);
        if (entity != null) {
            return entity;
        }
        throw new IllegalStateException("entity '" + entityName + "' could not be resolved");
    }
}
