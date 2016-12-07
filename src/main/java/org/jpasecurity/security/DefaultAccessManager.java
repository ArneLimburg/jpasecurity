/*
 * Copyright 2012 - 2016 Arne Limburg
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
package org.jpasecurity.security;

import static org.jpasecurity.util.Validate.notNull;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.SecurityContext;

/**
 * @author Arne Limburg
 */
public class DefaultAccessManager extends AbstractAccessManager {

    private EntityFilter entityFilter;

    public DefaultAccessManager(Metamodel metamodel, SecurityContext context, EntityFilter entityFilter) {
        super(metamodel, context);
        this.entityFilter = notNull(EntityFilter.class, entityFilter);
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        Object[] transientParameters = new Object[parameters.length];
        for (int i = 0; i < transientParameters.length; i++) {
            Object parameter = parameters[i];
            transientParameters[i] = parameter;
        }
        return super.isAccessible(accessType, entityName, transientParameters);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        if (entity == null) {
            return false;
        }
        return entityFilter.isAccessible(accessType, entity);
    }
}
