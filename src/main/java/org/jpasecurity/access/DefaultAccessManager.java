/*
 * Copyright 2013 - 2016 Arne Limburg
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
package org.jpasecurity.access;

import static org.jpasecurity.util.Validate.notNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;
import org.jpasecurity.util.ReflectionUtils;
import org.jpasecurity.util.SimpleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Limburg
 */
public class DefaultAccessManager implements AccessManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAccessManager.class);

    private Metamodel metamodel;
    private SecurityContext context;
    private AccessManager entityFilter;
    private boolean checkInProgress;
    private int checksDisabled;
    private int checksDelayed;
    private Map<Object, AccessType> entitiesToCheck = new SimpleMap<Object, AccessType>();

    public DefaultAccessManager(Metamodel metamodel, SecurityContext context, AccessManager filter) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.context = notNull(SecurityContext.class, context);
        this.entityFilter = notNull("EntityFilter", filter);
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        EntityType<?> classMapping = ManagedTypeFilter.forModel(metamodel).filter(entityName);
        Object entity = null;
        try {
            entity = ReflectionUtils.newInstance(classMapping.getJavaType(), parameters);
        } catch (RuntimeException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Constructor of " + classMapping.getJavaType()
                          + " threw exception, hence isAccessible returns false.", e);
            } else {
                LOG.info("Constructor of " + classMapping.getJavaType()
                         + " threw exception (\"" + e.getMessage() + "\"), hence isAccessible returns false.");
            }
            return false;
        }
        return isAccessible(accessType, entity);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        if (entity == null) {
            return false;
        }
        return entityFilter.isAccessible(accessType, entity);
    }

    public void checkAccess(AccessType accessType, Object entity) {
        notNull(AccessType.class, accessType);
        if (areChecksDisabled()) {
            return;
        }
        if (shouldCheckLater()) {
            checkLater(accessType, entity);
            return;
        }
        try {
            startCheck();
            if (!isAccessible(accessType, entity)) {
                throw new SecurityException("entity of type "
                        + entity.getClass().getSimpleName()
                        + " is not accessible with access type " + accessType);
            }
        } catch (SecurityException e) {
            abortCheck();
            throw e;
        }
        endCheck();
    }

    public SecurityContext getContext() {
        return context;
    }

    public boolean areChecksDisabled() {
        return checksDisabled > 0;
    }

    public void disableChecks() {
        checksDisabled++;
    }

    public void enableChecks() {
        checksDisabled--;
    }

    public boolean areChecksDelayed() {
        return checksDelayed > 0;
    }

    public void delayChecks() {
        checksDelayed++;
    }

    public void ignoreChecks(AccessType accessType, Collection<?> entities) {
        for (Object entity: entities) {
            AccessType type = entitiesToCheck.remove(entity);
            if (type != null && type != accessType) {
                entitiesToCheck.put(entity, type);
            }
        }
    }

    public void checkNow() {
        if (areChecksDelayed()) {
            checksDelayed--;
        }
        if (!areChecksDelayed() && !areChecksDisabled() && !entitiesToCheck.isEmpty()) {
            do {
                Iterator<Entry<Object, AccessType>> iterator = entitiesToCheck.entrySet().iterator();
                Entry<Object, AccessType> entry = iterator.next();
                AccessType accessType = entry.getValue();
                Object entity = entry.getKey();
                checkAccess(accessType, entity);
                entitiesToCheck.remove(entity);
            } while (!entitiesToCheck.isEmpty());
        }
    }

    private boolean shouldCheckLater() {
        return checkInProgress || areChecksDelayed();
    }

    private void checkLater(AccessType accessType, Object entity) {
        entitiesToCheck.put(entity, accessType);
    }

    private void startCheck() {
        checkInProgress = true;
    }

    private void abortCheck() {
        checkInProgress = false;
        entitiesToCheck.clear();
    }

    private void endCheck() {
        checkInProgress = false;
    }

    public abstract static class Instance {

        private static final Logger LOG = LoggerFactory.getLogger(AccessManager.class);

        private static Map<Thread, DefaultAccessManager> registeredAccessManagers
            = new ConcurrentHashMap<Thread, DefaultAccessManager>();

        public static DefaultAccessManager get() {
            DefaultAccessManager accessManager = registeredAccessManagers.get(Thread.currentThread());
            if (accessManager == null) {
                LOG.warn("No AccessManager found in thread {}", Thread.currentThread().getName());
                throw new SecurityException("No AccessManager available. Please ensure that the EntityManager is open");
            }
            return accessManager;
        }

        public static void register(DefaultAccessManager manager) {
            if (registeredAccessManagers.get(Thread.currentThread()) == manager) {
                return;
            }
            LOG.info("registering AccessManager#{}", System.identityHashCode(manager));
            registeredAccessManagers.values().remove(manager);
            registeredAccessManagers.put(Thread.currentThread(), manager);
        }

        public static void unregister(AccessManager manager) {
            LOG.info("unregistering AccessManager#{}", System.identityHashCode(manager));
            registeredAccessManagers.values().remove(manager);
        }
    }
}
