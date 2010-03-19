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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;

import net.sf.cglib.proxy.Callback;
import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
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
                           AccessManager accessManager,
                           SecureEntityProxyFactory proxyFactory) {
        super(mappingInformation, accessManager, proxyFactory);
        this.entityManager = entityManager;
    }

    public void persist(Object secureEntity) {
        Object unsecureEntity = createUnsecureObject(secureEntity);
        entityManager.persist(unsecureEntity);
        secureCopy(unsecureEntity, secureEntity);
        cascadeMergePersist(secureEntity, new HashSet<Object>());
    }

    public <T> T merge(T newEntity) {
        T unsecureEntity = createUnsecureObject(newEntity);
        T mergedEntity = entityManager.merge(unsecureEntity);
        T secureEntity = getSecureObject(mergedEntity);
        unsecureEntities.put(new SystemMapKey(secureEntity), mergedEntity);
        cascadeMergePersist(secureEntity, new HashSet<Object>());
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
        AccessType accessType = isNew(secureEntity)? AccessType.CREATE: AccessType.UPDATE;
        final ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        checkAccess(accessType, secureEntity);
        Object unsecureEntity = classMapping.newInstance();
        secureEntities.put(new SystemMapKey(unsecureEntity), secureEntity);
        unsecureEntities.put(new SystemMapKey(secureEntity), unsecureEntity);
        unsecureCopy(accessType, secureEntity, unsecureEntity);
        return (T)unsecureEntity;
    }

    private boolean isNew(Object entity) {
        final ClassMappingInformation classMapping = getClassMapping(entity.getClass());
        Object id = classMapping.getId(entity);
        if (id == null) {
            return true;
        }
        return entityManager.find(classMapping.getEntityType(), id) == null;
    }

    private void cascadeMergePersist(Object entity, Set<Object> alreadyCascadedEntities) {
        if (entity == null || alreadyCascadedEntities.contains(entity)) {
            return;
        }
        alreadyCascadedEntities.add(entity);
        ClassMappingInformation classMapping = getClassMapping(entity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (!propertyMapping.isRelationshipMapping()) {
                continue;
            }
            RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
            if (relationshipMapping.getCascadeTypes().contains(CascadeType.ALL)
             || relationshipMapping.getCascadeTypes().contains(CascadeType.MERGE)
             || relationshipMapping.getCascadeTypes().contains(CascadeType.PERSIST)) {
                if (relationshipMapping instanceof CollectionValuedRelationshipMappingInformation) {
                    Collection<Object> collection = (Collection<Object>)relationshipMapping.getPropertyValue(entity);
                    if (collection != null) {
                        for (Object entry: collection.toArray()) {
                            Object mergedEntry = entry;
                        if (entry instanceof SecureEntity) {
                            mergedEntry = ((SecureEntity)entry).merge(entityManager, this);
                        }
                        if (entry != mergedEntry) {
                            replace(collection, entry, mergedEntry);
                        }
                        cascadeMergePersist(mergedEntry, alreadyCascadedEntities);
                    }
                    }
                } else {
                    Object originalValue = relationshipMapping.getPropertyValue(entity);
                    Object mergedValue = originalValue;
                    if (originalValue instanceof SecureEntity) {
                        mergedValue = ((SecureEntity)originalValue).merge(entityManager, this);
                    }
                    if (originalValue != mergedValue) {
                        relationshipMapping.setPropertyValue(entity, mergedValue);
                    }
                    cascadeMergePersist(mergedValue, alreadyCascadedEntities);
                }
            }
        }
    }

    private void replace(Collection<Object> collection, Object oldValue, Object newValue) {
        if (collection instanceof List) {
            int index = ((List<Object>)collection).indexOf(oldValue);
            ((List<Object>)collection).set(index, newValue);
        } else {
            collection.remove(oldValue);
            collection.add(newValue);
        }
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
