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

import static net.sf.jpasecurity.util.Types.isSimplePropertyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.LockModeType;
import net.sf.jpasecurity.Parameter;
import net.sf.jpasecurity.Parameterizable;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.util.SystemIdentity;

/**
 * @author Arne Limburg
 */
public class DefaultSecureObjectManager extends AbstractSecureObjectManager {

    protected final BeanStore beanStore;
    protected final SecureObjectLoader secureObjectLoader;
    private Map<SystemIdentity, Object> secureEntities = new HashMap<SystemIdentity, Object>();
    private Map<SystemIdentity, Object> unsecureEntities = new HashMap<SystemIdentity, Object>();

    public DefaultSecureObjectManager(MappingInformation mappingInformation,
                                      BeanStore beanStore,
                                      AccessManager accessManager) {
        this(mappingInformation, beanStore, accessManager, new Configuration());
    }

    public DefaultSecureObjectManager(MappingInformation mappingInformation,
                                      BeanStore beanStore,
                                      AccessManager accessManager,
                                      Configuration configuration) {
        super(mappingInformation, accessManager, configuration);
        this.beanStore = beanStore;
        this.secureObjectLoader = new DefaultSecureObjectLoader(beanStore, this);
    }

    public Object getIdentifier(Object entity) {
        return secureObjectLoader.getIdentifier(entity);
    }

    public boolean isLoaded(Object object) {
        return secureObjectLoader.isLoaded(object);
    }

    public boolean isLoaded(Object object, String property) {
        return secureObjectLoader.isLoaded(object, property);
    }

    public void persist(Object secureEntity) {
        Object unsecureEntity = getUnsecureObject(secureEntity);
        cascade(secureEntity, unsecureEntity, CascadeType.PERSIST, new HashSet<SystemIdentity>());
        preFlush();
        beanStore.persist(unsecureEntity);
        postFlush();
    }

    public <T> T merge(T entity) {
        boolean isNew = isNew(entity);
        preFlush();
        T unsecureEntity = getUnsecureObject(entity);
        if (isNew) {
            cascade(entity, unsecureEntity, CascadeType.MERGE, new HashSet<SystemIdentity>());
        }
        executePreFlushOperations();
        unsecureEntity = beanStore.merge(unsecureEntity);
        postFlush();
        if (!isNew) {
            cascade(entity, unsecureEntity, CascadeType.MERGE, new HashSet<SystemIdentity>());
        }
        T secureEntity = getSecureObject(unsecureEntity);
        initialize(secureEntity, unsecureEntity, isNew, CascadeType.MERGE, new HashSet<Object>());
        if (isNew) {
            unsecureEntities.put(new SystemIdentity(secureEntity), unsecureEntity);
        }
        return secureEntity;
    }

    public boolean contains(Object entity) {
        return beanStore.contains(getUnsecureObject(entity));
    }

    public void refresh(Object bean) {
        refresh(bean, null, null);
    }

    public void refresh(Object bean, LockModeType lockMode) {
        refresh(bean, lockMode, null);
    }

    public void refresh(Object bean, Map<String, Object> properties) {
        refresh(bean, null, properties);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        checkAccess(AccessType.READ, entity);
        Object unsecureEntity = getUnsecureObject(entity);
        preFlush();
        if (lockMode != null && properties != null) {
            beanStore.refresh(unsecureEntity, lockMode, properties);
        } else if (lockMode != null) {
            beanStore.refresh(unsecureEntity, lockMode);
        } else if (properties != null) {
            beanStore.refresh(unsecureEntity, properties);
        } else {
            beanStore.refresh(unsecureEntity);
        }
        postFlush();
        if (entity instanceof SecureEntity) {
            initialize((SecureEntity)entity, true);
        }
        cascadeRefresh(entity, unsecureEntity, new HashSet<SystemIdentity>());
    }

    public void lock(Object entity, LockModeType lockMode) {
        lock(entity, lockMode, null);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        if (lockMode == LockModeType.READ && !isAccessible(AccessType.READ, entity)) {
            throw new SecurityException("specified entity is not readable for locking");
        } else if (lockMode == LockModeType.WRITE && !isAccessible(AccessType.UPDATE, entity)) {
            throw new SecurityException("specified entity is not updateable for locking");
        }
        if (properties != null) {
            beanStore.lock(getUnsecureObject(entity), lockMode, properties);
        } else {
            beanStore.lock(getUnsecureObject(entity), lockMode);
        }
    }


    public LockModeType getLockMode(Object entity) {
        return beanStore.getLockMode(getUnsecureObject(entity));
    }

    public void remove(Object entity) {
        checkAccess(AccessType.DELETE, entity);
        Object unsecureEntity = getUnsecureObject(entity);
        cascadeRemove(entity, unsecureEntity, new HashSet<SystemIdentity>());
        if (entity instanceof SecureEntity) {
            setRemoved((SecureEntity)entity);
        }
        executePreFlushOperations();
        beanStore.remove(unsecureEntity);
    }

    public void detach(Object secureBean) {
        Object unsecureBean = getUnsecureObject(secureBean);
        unsecureEntities.remove(new SystemIdentity(secureBean));
        secureEntities.remove(new SystemIdentity(unsecureBean));
        beanStore.detach(unsecureBean);
    }

    public <P extends Parameterizable> P setParameter(P parameterizable, int index, Object value) {
        parameterizable.setParameter(index, convertParameter(value));
        return parameterizable;
    }

    public <P extends Parameterizable> P setParameter(P parameterizable, String name, Object value) {
        parameterizable.setParameter(name, convertParameter(value));
        return parameterizable;
    }

    public <P extends Parameterizable, T> P setParameter(P parameterizable, Parameter<T> parameter, T value) {
        parameterizable.setParameter(parameter, convertParameter(value));
        return null;
    }

    private <T> T convertParameter(T value) {
        if (value == null || isSimplePropertyType(value.getClass())) {
            return value;
        } else if (value instanceof Collection) {
            Collection<Object> parameter = new ArrayList<Object>();
            for (Object entry: (Collection<?>)value) {
                if (isSimplePropertyType(entry.getClass())) {
                    parameter.add(entry);
                } else {
                    parameter.add(convertParameter(entry));
                }
            }
            return (T)parameter;
        } else if (containsUnsecureObject(value)) {
            return getUnsecureObject(value);
        } else {
            ClassMappingInformation classMapping = getClassMapping(value.getClass());
            Object id = classMapping.getId(value);
            if (id == null) { //TODO correctly handle unsaved values
                return value;
            }
            Object managedValue = beanStore.find(classMapping.getEntityType(), id);
            return (T)(managedValue == null? value: managedValue);
        }
    }

    public void preFlush() {
        Collection<Map.Entry<SystemIdentity, Object>> entities = unsecureEntities.entrySet();
        for (Map.Entry<SystemIdentity, Object> unsecureEntity: entities.toArray(new Map.Entry[entities.size()])) {
            unsecureCopy(AccessType.UPDATE, unsecureEntity.getKey().getObject(), unsecureEntity.getValue());
        }
        executePreFlushOperations();
    }

    public void postFlush() {
        //copy over ids and version ids
        for (Map.Entry<SystemIdentity, Object> secureEntity: secureEntities.entrySet()) {
            copyIdAndVersion(secureEntity.getKey().getObject(), secureEntity.getValue());
        }
        super.postFlush();
    }

    public void clear() {
        secureEntities.clear();
        unsecureEntities.clear();
    }

    public boolean isSecureObject(Object object) {
        if (super.isSecureObject(object)) {
            return true;
        }
        object = unwrap(object);
        return unsecureEntities.containsKey(new SystemIdentity(object));
    }

    public <E> Collection<E> getSecureObjects(Class<E> type) {
        List<E> result = new ArrayList<E>();
        for (Object secureEntity: secureEntities.values()) {
            if (type.isInstance(secureEntity)) {
                result.add(type.cast(secureEntity));
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    public <T> T getReference(Class<T> type, Object id) {
        return getSecureObject(beanStore.getReference(type, id));
    }

    public <T> T getSecureObject(T unsecureObject) {
        if (unsecureObject == null) {
            return null;
        }
        T secureEntity = (T)secureEntities.get(new SystemIdentity(unsecureObject));
        if (secureEntity != null) {
            return secureEntity;
        }
        return (T)super.getSecureObject(unsecureObject);
    }

    boolean containsUnsecureObject(Object secureObject) {
        if (secureObject == null) {
            return true;
        }
        secureObject = unwrap(secureObject);
        if (unsecureEntities.containsKey(new SystemIdentity(secureObject))) {
            return true;
        }
        return super.containsUnsecureObject(secureObject);
    }

    <T> T getUnsecureObject(T secureObject) {
        if (secureObject == null) {
            return null;
        }
        secureObject = unwrap(secureObject);
        Object unsecureEntity = unsecureEntities.get(new SystemIdentity(secureObject));
        if (unsecureEntity != null) {
            return (T)unsecureEntity;
        }
        return super.getUnsecureObject(secureObject);
    }

    <T> T createUnsecureObject(T secureEntity) {
        secureEntity = unwrap(secureEntity);
        AccessType accessType = isNew(secureEntity)? AccessType.CREATE: AccessType.UPDATE;
        final ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        checkAccess(accessType, secureEntity);
        Object unsecureEntity = classMapping.newInstance();
        secureEntities.put(new SystemIdentity(unsecureEntity), secureEntity);
        unsecureEntities.put(new SystemIdentity(secureEntity), unsecureEntity);
        unsecureCopy(accessType, secureEntity, unsecureEntity);
        copyIdAndVersion(secureEntity, unsecureEntity);
        return (T)unsecureEntity;
    }

    boolean isNew(Object entity) {
        if (entity instanceof SecureEntity) {
            return false;
        }
        final ClassMappingInformation classMapping = getClassMapping(entity.getClass());
        Object id = classMapping.getId(entity);
        if (id == null) {
            return true;
        }
        return beanStore.find(classMapping.getEntityType(), id) == null;
    }

    void cascade(Object secureEntity,
                 Object unsecureEntity,
                 CascadeType cascadeType,
                 Set<SystemIdentity> alreadyCascadedEntities) {
        if (cascadeType == CascadeType.REMOVE) {
            cascadeRemove(secureEntity, unsecureEntity, alreadyCascadedEntities);
            return;
        }
        if (secureEntity == null || alreadyCascadedEntities.contains(new SystemIdentity(secureEntity))) {
            return;
        }
        alreadyCascadedEntities.add(new SystemIdentity(secureEntity));
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
                        CollectionValuedRelationshipMappingInformation collectionMapping
                            = (CollectionValuedRelationshipMappingInformation)propertyMapping;
                        if (Collection.class.isAssignableFrom(collectionMapping.getCollectionType())) {
                            for (Object secureEntry: ((Collection<Object>)secureValue)) {
                                Object unsecureEntry = getUnsecureObject(secureEntry);
                                cascade(secureEntry, unsecureEntry, cascadeType, alreadyCascadedEntities);
                            }
                        } else if (Map.class.isAssignableFrom(collectionMapping.getCollectionType())) {
                            for (Object secureEntry: ((Map<Object, Object>)secureValue).values()) {
                                Object unsecureEntry = getUnsecureObject(secureEntry);
                                cascade(secureEntry, unsecureEntry, cascadeType, alreadyCascadedEntities);
                            }
                        }
                    }
                }
            }
        }
    }

    private void cascadeRemove(Object secureEntity,
                               Object unsecureEntity,
                               Set<SystemIdentity> alreadyCascadedEntities) {
        if (secureEntity == null || alreadyCascadedEntities.contains(new SystemIdentity(secureEntity))) {
            return;
        }
        alreadyCascadedEntities.add(new SystemIdentity(secureEntity));
        checkAccess(AccessType.DELETE, secureEntity);
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        fireRemove(classMapping, secureEntity);
        secureEntities.remove(new SystemIdentity(unsecureEntity));
        unsecureEntities.remove(new SystemIdentity(secureEntity));
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
                        Object unsecureValue = propertyMapping.getPropertyValue(unsecureEntity);
                        if (unsecureValue instanceof Collection) {
                            Collection<Object> unsecureCollection = (Collection<Object>)unsecureValue;
                            for (Object unsecureEntry: unsecureCollection) {
                                cascadeRemove(getSecureObject(unsecureEntry), unsecureEntry, alreadyCascadedEntities);
                            }
                        } else if (unsecureValue instanceof Map) {
                            Map<Object, Object> unsecureMap = (Map<Object, Object>)unsecureValue;
                            for (Object unsecureEntry: unsecureMap.values()) {
                                cascadeRemove(getSecureObject(unsecureEntry), unsecureEntry, alreadyCascadedEntities);
                            }
                        } else {
                            throw new IllegalStateException("unsupported type " + unsecureValue.getClass().getName());
                        }
                    }
                }
            }
        }
    }

    private void cascadeRefresh(Object secureEntity,
                                Object unsecureEntity,
                                Set<SystemIdentity> alreadyCascadedEntities) {
        if (secureEntity == null || alreadyCascadedEntities.contains(new SystemIdentity(secureEntity))) {
            return;
        }
        alreadyCascadedEntities.add(new SystemIdentity(secureEntity));
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping()
                && (propertyMapping.getCascadeTypes().contains(CascadeType.REFRESH)
                    || propertyMapping.getCascadeTypes().contains(CascadeType.ALL))) {
                Object secureValue = propertyMapping.getPropertyValue(secureEntity);
                if (secureValue != null) {
                    if (propertyMapping.isSingleValued()) {
                        Object unsecureValue = getUnsecureObject(secureValue);
                        if (secureValue instanceof SecureEntity) {
                            initialize((SecureEntity)secureValue, true);
                        } else {
                            secureCopy(unsecureValue, secureValue);
                        }
                        cascadeRefresh(secureValue, unsecureValue, alreadyCascadedEntities);
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
                            boolean isNew,
                            CascadeType cascadeType,
                            Set<Object> alreadyInitializedEntities) {
        if (alreadyInitializedEntities.contains(secureEntity)) {
            return;
        }
        if (secureEntity instanceof SecureEntity && !((SecureEntity)secureEntity).isInitialized()) {
            initialize((SecureEntity)secureEntity, !isNew);
        }
        alreadyInitializedEntities.add(secureEntity);
        ClassMappingInformation classMapping = getClassMapping(secureEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping()
                && (propertyMapping.getCascadeTypes().contains(cascadeType)
                    || propertyMapping.getCascadeTypes().contains(CascadeType.ALL))) {
                Object secureValue = propertyMapping.getPropertyValue(secureEntity);
                if (secureValue != null) {
                    if (propertyMapping.isSingleValued()) {
                        initialize(secureValue,
                                   getUnsecureObject(secureValue),
                                   isNew(secureValue),
                                   cascadeType,
                                   alreadyInitializedEntities);
                    } else {
                        if (secureValue instanceof AbstractSecureCollection) {
                            AbstractSecureCollection<?, Collection<?>> secureCollection
                                = (AbstractSecureCollection<?, Collection<?>>)secureValue;
                            secureCollection.initialize(!isNew);
                        } else if (secureValue instanceof SecureList) {
                            SecureList<?> secureList = (SecureList<?>)secureValue;
                            secureList.initialize(!isNew);
                        } else if (secureValue instanceof DefaultSecureMap) {
                            DefaultSecureMap<?, ?> secureMap = (DefaultSecureMap<?, ?>)secureValue;
                            secureMap.initialize(!isNew);
                        }
                        if (secureValue instanceof Collection) {
                            for (Object secureEntry: ((Collection<Object>)secureValue)) {
                                initialize(secureEntry,
                                           getUnsecureObject(secureEntry),
                                           isNew(secureEntity),
                                           cascadeType,
                                           alreadyInitializedEntities);
                            }
                        } else if (secureValue instanceof Map) {
                            for (Object secureEntry: ((Map<Object, Object>)secureValue).values()) {
                                initialize(secureEntry,
                                           getUnsecureObject(secureEntry),
                                           isNew(secureEntity),
                                           cascadeType,
                                           alreadyInitializedEntities);
                            }
                        } else {
                            throw new IllegalStateException("unsupported type " + secureValue.getClass().getName());
                        }
                    }
                }
            }
        }
    }
}
