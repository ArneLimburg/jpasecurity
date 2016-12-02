/*
 * Copyright 2012 Arne Limburg
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

import org.jpasecurity.AccessType;
import org.jpasecurity.entity.AbstractSecureObjectManager;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.proxy.EntityProxy;
import org.jpasecurity.util.DoubleKeyHashMap;

import static org.jpasecurity.util.Validate.notNull;

/**
 * @author Arne Limburg
 */
public class DefaultAccessManager extends AbstractAccessManager {

    private MappingInformation mappingInformation;
    private AbstractSecureObjectManager objectManager;
    private EntityFilter entityFilter;
    private DoubleKeyHashMap<ClassMappingInformation, Object, Boolean> cachedReadAccess
        = new DoubleKeyHashMap<ClassMappingInformation, Object, Boolean>();

    public DefaultAccessManager(MappingInformation mappingInformation,
                                AbstractSecureObjectManager objectManager,
                                EntityFilter entityFilter) {
        super(mappingInformation);
        notNull(MappingInformation.class, mappingInformation);
        notNull(AbstractSecureObjectManager.class, objectManager);
        notNull(EntityFilter.class, entityFilter);
        this.mappingInformation = mappingInformation;
        this.objectManager = objectManager;
        this.entityFilter = entityFilter;
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        Object[] transientParameters = new Object[parameters.length];
        for (int i = 0; i < transientParameters.length; i++) {
            Object parameter = parameters[i];
            if (parameter instanceof EntityProxy) {
                parameter = ((EntityProxy)parameter).getEntity();
            }
            if (parameter != null && mappingInformation.containsClassMapping(parameter.getClass())) {
                transientParameters[i] = objectManager.createSecureEntity(parameter, true);
            } else {
                transientParameters[i] = parameter;
            }
        }
        return super.isAccessible(accessType, entityName, transientParameters);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        if (entity instanceof EntityProxy) {
            entity = ((EntityProxy)entity).getEntity();
        }
        if (entity == null) {
            return false;
        }
        final ClassMappingInformation classMapping = mappingInformation.getClassMapping(entity.getClass());
        final Object entityId = classMapping.getId(entity);
        if (accessType == AccessType.READ) {
            final Boolean isAccessible = cachedReadAccess.get(classMapping, entityId);
            if (isAccessible != null) {
                return isAccessible;
            }
        }
        try {
            final boolean accessible = entityFilter.isAccessible(accessType, entity);
            if (accessType == AccessType.READ) {
                cachedReadAccess.put(classMapping, entityId, accessible);
            }
            return accessible;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
