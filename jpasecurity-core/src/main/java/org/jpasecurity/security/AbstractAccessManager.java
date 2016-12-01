/*
 * Copyright 2013 Arne Limburg
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public abstract class AbstractAccessManager implements AccessManager {

    private static final Log LOG = LogFactory.getLog(AbstractAccessManager.class);

    private MappingInformation mappingInformation;

    public AbstractAccessManager(MappingInformation mappingInformation) {
        this.mappingInformation = notNull(MappingInformation.class, mappingInformation);
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(entityName);
        Object entity = null;
        try {
            entity = ReflectionUtils.newInstance(classMapping.getEntityType(), parameters);
        } catch (RuntimeException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Constructor of " + classMapping.getEntityType()
                          + " threw exception, hence isAccessible returns false.", e);
            } else {
                LOG.info("Constructor of " + classMapping.getEntityType()
                         + " threw exception (\"" + e.getMessage() + "\"), hence isAccessible returns false.");
            }
            return false;
        }
        return isAccessible(accessType, entity);
    }
}
