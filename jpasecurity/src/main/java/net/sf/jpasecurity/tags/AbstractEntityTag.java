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
package net.sf.jpasecurity.tags;

import net.sf.jpasecurity.AccessType;

/**
 * A tag that renders the jsp body only if a specified entity is accessible.
 * @author Arne Limburg
 */
public abstract class AbstractEntityTag extends AbstractSecurityTag {

    private Object entity;

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    protected boolean isAccessible() {
        return getAccessChecker().isAccessible(getEntity(), getAccessType());
    }

    protected abstract AccessType getAccessType();
}
