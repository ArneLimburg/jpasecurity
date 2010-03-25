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
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
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
        Object unsecureEntity = getUnsecureObject(secureEntity);
        cascade(secureEntity, unsecureEntity, CascadeType.PERSIST, new HashSet<Object>());
        entityManager.persist(unsecureEntity);
        copyIdAndVersion(unsecureEntity, secureEntity);
    }

    public <T> T merge(T newEntity) {
        T unsecureEntity = getUnsecureObject(newEntity);
        cascade(newEntity, unsecureEntity, CascadeType.MERGE, new HashSet<Object>());
        T mergedEntity = entityManager.merge(unsecureEntity);
        T secureEntity = getSecureObject(mergedEntity);
        initialize(secureEntity, mergedEntity, CascadeType.MERGE, new HashSet<Object>());
        unsecureEntities.put(new SystemMapKey(secureEntity), mergedEntity);
        return secureEntity;
    }

    public void removeNew(final Object newEntity) {
        checkAccess(AccessType.DELETE, newEntity);
        Object unsecureEntity = getUnsecureObject(newEntity);
        cascadeRemove(newEntity, unsecureEntity, new HashSet<Object>());
        entityManager.remove(unsecureEntity);
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
            copyIdAndVersion(secureEntity.getKey().getObject(), secureEntity.getValue());
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

    boolean isNew(Object entity) {
        final ClassMappingInformation classMapping = getClassMapping(entity.getClass());
        Object id = classMapping.getId(entity);
        if (id == null) {
            return true;
        }
        return entityManager.find(classMapping.getEntityType(), id) == null;
    }

    void cascade(Object secureEntity,
                 Object unsecureEntity,
                 CascadeType cascadeType,
                 Set<Object> alreadyCascadedEntities) {
        if (cascadeType == CascadeType.REMOVE) {
            cascadeRemove(secureEntity, unsecureEntity, alreadyCascadedEntities);
            return;
        }
        if (secureEntity == null || alreadyCascadedEntities.contains(secureEntity)) {
            return;
        }
        alreadyCascadedEntities.add(secureEntity);
        AccessType accessType = isNew(secureEntity)? AccessType.CREATE: AccessType.UPDATE;
        unsecureCopy(accessType, secureEntity, unsecureEntity);
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
           if (propertyMapping.isRelationshipMapping()
               && (propertyMapping.getCascadeTypes().contains(cascadeType)
                   || propertyMapping.getCascadeTypes().contains(CascadeType.ALL))) {
               Object secureValue = propertyMapping.getPropertyValue(secureEntity);
               if (secureValue != null) {
                  if (propertyMapping.isSingleValued()) {
                      cascade(secureValue, getUnsecureObject(secureValue), cascadeType, alreadyCascadedEntities);
                  } else {
                      for (Object secureEntry: ((Collection<Object>)secureValue)) {
                          cascade(secureEntry, getUnsecureObject(secureEntry), cascadeType, alreadyCascadedEntities);
                      }
                  }
               }
           }
        }
    }

    private void cascadeRemove(Object secureEntity, Object unsecureEntity, Set<Object> alreadyCascadedEntities) {
        if (secureEntity == null || alreadyCascadedEntities.contains(secureEntity)) {
            return;
        }
        alreadyCascadedEntities.add(secureEntity);
        checkAccess(AccessType.DELETE, secureEntity);
        fireRemove(getClassMapping(secureEntity.getClass()), secureEntity);
        secureEntities.remove(new SystemMapKey(unsecureEntity));
        unsecureEntities.remove(new SystemMapKey(secureEntity));
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping()
                && (propertyMapping.getCascadeTypes().contains(CascadeType.REMOVE)
                    || propertyMapping.getCascadeTypes().contains(CascadeType.ALL))) {
                Object secureValue = propertyMapping.getPropertyValue(secureEntity);
                if (secureValue != null) {
                    if (propertyMapping.isSingleValued()) {
                        cascadeRemove(secureValue, getUnsecureObject(secureValue), alreadyCascadedEntities);
                        if (secureValue instanceof SecureEntity) {
                            setRemoved((SecureEntity)secureValue);
                        }
                    } else {
                        //use the unsecure collection here since the secure may be filtered
                        Collection<Object> unsecureCollection
                            = (Collection<Object>)propertyMapping.getPropertyValue(unsecureEntity);
                        for (Object unsecureEntry: unsecureCollection) {
                            cascadeRemove(getSecureObject(unsecureEntry), unsecureEntry, alreadyCascadedEntities);
                        }
                    }
                }
            }
        }
    }

    private void initialize(Object secureEntity,
                            Object unsecureEntity,
                            CascadeType cascadeType,
                            Set<Object> alreadyInitializedEntities) {
        if (alreadyInitializedEntities.contains(secureEntity)) {
            return;
        }
        alreadyInitializedEntities.add(secureEntity);
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping()
                && (propertyMapping.getCascadeTypes().contains(cascadeType)
                    || propertyMapping.getCascadeTypes().contains(CascadeType.ALL))) {
                Object secureValue = propertyMapping.getPropertyValue(secureEntity);
                if (propertyMapping.isSingleValued()) {
                    initialize(secureValue, getUnsecureObject(secureValue), cascadeType, alreadyInitializedEntities);
                } else {
                    for (Object secureEntry: ((Collection<Object>)secureValue)) {
                        initialize(secureEntry,
                                   getUnsecureObject(secureEntry),
                                   cascadeType,
                                   alreadyInitializedEntities);
                    }
                }
            }
        }
    }

//    private void cascadeMergePersist(Object entity, Set<Object> alreadyCascadedEntities) {
//        if (entity == null || alreadyCascadedEntities.contains(entity)) {
//            return;
//        }
//        alreadyCascadedEntities.add(entity);
//        ClassMappingInformation classMapping = getClassMapping(entity.getClass());
//        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
//            if (!propertyMapping.isRelationshipMapping()) {
//                continue;
//            }
//            RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
//            if (relationshipMapping.getCascadeTypes().contains(CascadeType.ALL)
//             || relationshipMapping.getCascadeTypes().contains(CascadeType.MERGE)
//             || relationshipMapping.getCascadeTypes().contains(CascadeType.PERSIST)) {
//                if (relationshipMapping instanceof CollectionValuedRelationshipMappingInformation) {
//                    Collection<Object> collection = (Collection<Object>)relationshipMapping.getPropertyValue(entity);
//                    if (collection != null) {
//                        for (Object entry: collection.toArray()) {
//                            Object mergedEntry = entry;
//                        if (entry instanceof SecureEntity) {
//                            mergedEntry = ((SecureEntity)entry).merge(entityManager, this)//                        }
//                        if (entry != mergedEntry) {
//                            replace(collection, entry, mergedEntry);
//                        }
//                        cascadeMergePersist(mergedEntry, alreadyCascadedEntities);
//                    }
//                    }
//                } else {
//                    Object originalValue = relationshipMapping.getPropertyValue(entity);
//                    Object mergedValue = originalValue;
//                    if (originalValue instanceof SecureEntity) {
//                        mergedValue = ((SecureEntity)originalValue).merge(entityManager, this);
//                    }
//                    if (originalValue != mergedValue) {
//                        relationshipMapping.setPropertyValue(entity, mergedValue);
//                    }
//                    cascadeMergePersist(mergedValue, alreadyCascadedEntities);
//                }
//            }
//        }
//    }

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
