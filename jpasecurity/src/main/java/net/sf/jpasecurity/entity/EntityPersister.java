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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import net.sf.cglib.proxy.Callback;
import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.util.ReflectionUtils;
import net.sf.jpasecurity.util.SystemMapKey;

/**
 * @author Arne Limburg
 */
public class EntityPersister extends AbstractSecureObjectManager {

    private EntityManager entityManager;
    private Map<SystemMapKey, Object> secureEntities = new HashMap<SystemMapKey, Object>();
    private Map<SystemMapKey, Object> unsecureEntities = new HashMap<SystemMapKey, Object>();

    public EntityPersister(MappingInformation mappingInformation,
                           EntityManager entityManager,
                           AccessManager accessManager) {
        super(mappingInformation, accessManager);
        this.entityManager = entityManager;
    }

    public void persist(Object secureEntity) {
        Object unsecureEntity = createUnsecureObject(secureEntity);
        entityManager.persist(unsecureEntity);
        secureCopy(unsecureEntity, secureEntity);
    }

    public <T> T merge(T entity) {
        final ClassMappingInformation classMapping = getClassMapping(entity.getClass());
        Object id = classMapping.getId(entity);
        Object unsecureEntity = entityManager.find(classMapping.getEntityType(), id);
        if (unsecureEntity == null) {
            return mergeNew(entity);
        }
        final boolean dirty = isDirty(unsecureEntity, entity);
        final T mergedEntity = entityManager.merge(entity);
        final T secureEntity = getSecureObject(mergedEntity);
        if (dirty) {
            classMapping.preUpdate(secureEntity);
            addPostFlushOperation(new Runnable() {
                public void run() {
                    classMapping.postUpdate(secureEntity);
                }
            });
        }
        return secureEntity;
    }

    public <T> T mergeNew(T newEntity) {
        checkAccess(AccessType.CREATE, newEntity);
        T mergedEntity = entityManager.merge(newEntity);
        final T secureEntity = getSecureObject(mergedEntity);
        initialize((SecureEntity)secureEntity);
        final ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        classMapping.prePersist(secureEntity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postPersist(secureEntity);
            }
        });
        unsecureEntities.put(new SystemMapKey(secureEntity), mergedEntity);
        return secureEntity;
    }

    public void removeNew(final Object newEntity) {
        checkAccess(AccessType.DELETE, newEntity);
        final ClassMappingInformation classMapping = getClassMapping(newEntity.getClass());
        classMapping.preRemove(newEntity);
        Object unsecureEntity = getUnsecureObject(newEntity);
        entityManager.remove(unsecureEntity);
        secureEntities.remove(new SystemMapKey(unsecureEntity));
        unsecureEntities.remove(new SystemMapKey(newEntity));
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postRemove(newEntity);
            }
        });
        //TODO cascade remove
    }

    public void preFlush() {
        Collection<Map.Entry<SystemMapKey, Object>> entities = unsecureEntities.entrySet();
        for (Map.Entry<SystemMapKey, Object> unsecureEntity: entities.toArray(new Map.Entry[entities.size()])) {
            unsecureCopy(AccessType.UPDATE, unsecureEntity.getKey().getObject(), unsecureEntity.getValue());
        }
    }

    public void postFlush() {
        //copy over ids and version ids
        for (Map.Entry<SystemMapKey, Object> secureEntity: secureEntities.entrySet()) {
            ClassMappingInformation classMapping = getClassMapping(secureEntity.getValue().getClass());
            for (PropertyMappingInformation propertyMapping: classMapping.getIdPropertyMappings()) {
                Object newId = propertyMapping.getPropertyValue(secureEntity.getKey().getObject());
                propertyMapping.setPropertyValue(secureEntity.getValue(), newId);
            }
            for (PropertyMappingInformation propertyMapping: classMapping.getVersionPropertyMappings()) {
                Object newVersion = propertyMapping.getPropertyValue(secureEntity.getKey().getObject());
                propertyMapping.setPropertyValue(secureEntity.getValue(), newVersion);
            }
        }
        super.postFlush();
    }

    public void clear() {
        secureEntities.clear();
        unsecureEntities.clear();
    }

    public boolean isSecureObject(Object object) {
        return super.isSecureObject(object) || unsecureEntities.containsKey(new SystemMapKey(object));
    }

    public <E> Collection<E> getSecureObjects(Class<E> type) {
        List<E> result = new ArrayList<E>();
        for (Object secureEntity: secureEntities.values()) {
            if (type.isInstance(secureEntity)) {
                result.add((E)secureEntity);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    public <T> T getSecureObject(T unsecureObject) {
        if (unsecureObject == null) {
            return null;
        }
        T secureEntity = (T)secureEntities.get(new SystemMapKey(unsecureObject));
        if (secureEntity != null) {
            return secureEntity;
        }
        return (T)super.getSecureObject(unsecureObject);
    }

    <T> T getUnsecureObject(T secureObject) {
        if (secureObject == null) {
            return null;
        }
        Object unsecureEntity = unsecureEntities.get(new SystemMapKey(secureObject));
        if (unsecureEntity != null) {
            return (T)unsecureEntity;
        }
        return super.getUnsecureObject(secureObject);
    }

    <T> T createUnsecureObject(final T secureEntity) {
        checkAccess(AccessType.CREATE, secureEntity);
        final ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        classMapping.prePersist(secureEntity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postPersist(secureEntity);
            }
        });
        Object unsecureEntity = classMapping.newInstance();
        secureEntities.put(new SystemMapKey(unsecureEntity), secureEntity);
        unsecureEntities.put(new SystemMapKey(secureEntity), unsecureEntity);
        unsecureCopy(AccessType.CREATE, secureEntity, unsecureEntity);
        return (T)unsecureEntity;
    }

    /**
     * This method initializes the specified secure entity without calling postLoad
     */
    private void initialize(SecureEntity secureEntity) {
        try {
            for (Callback callback: (Callback[])ReflectionUtils.invokeMethod(secureEntity, "getCallbacks")) {
                if (callback instanceof EntityInvocationHandler) {
                    ((EntityInvocationHandler)callback).initialize();
                }
            }
        } catch (SecurityException e) {
            // ignore
        }
    }
}
