/*
 * Copyright 2010 - 2011 Arne Limburg
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

import static net.sf.jpasecurity.util.JpaTypes.isSimplePropertyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.SecureObject;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.proxy.Decorator;
import net.sf.jpasecurity.proxy.EntityProxy;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.util.SystemMapKey;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectManager implements SecureObjectManager {

    private final MappingInformation mappingInformation;
    private final AccessManager accessManager;
    private final Configuration configuration;
    private final List<Runnable> postFlushOperations = new ArrayList<Runnable>();

    public AbstractSecureObjectManager(MappingInformation mappingInformation,
                                       AccessManager accessManager,
                                       Configuration configuration) {
        this.mappingInformation = mappingInformation;
        this.accessManager = accessManager;
        this.configuration = configuration;
    }

    protected void addPostFlushOperation(Runnable operation) {
        postFlushOperations.add(operation);
    }

    public void postFlush() {
        try {
            for (Runnable operation: postFlushOperations) {
                operation.run();
            }
        } finally {
            postFlushOperations.clear();
        }
    }

    public boolean isSecureObject(Object object) {
        return object instanceof SecureObject;
    }

    public <T> T getSecureObject(T object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List) {
            return (T)createSecureList((List<?>)object, this, accessManager);
        } else if (object instanceof SortedSet) {
            return (T)createSecureSortedSet((SortedSet<?>)object, this, accessManager);
        } else if (object instanceof Set) {
            return (T)createSecureSet((Set<?>)object, this, accessManager);
        } else if (object instanceof Collection) {
            return (T)createSecureCollection((Collection<?>)object, this, accessManager);
        } else if (object instanceof Map) {
            return (T)createSecureMap((Map<?, ?>)object, this, accessManager);
        } else {
            ClassMappingInformation mapping = getClassMapping(object.getClass());
            BeanInitializer beanInitializer = configuration.getBeanInitializer();
            SecureEntityInterceptor interceptor = new SecureEntityInterceptor(beanInitializer, this, object);
            Decorator<SecureEntity> decorator
                = new SecureEntityDecorator(mapping, beanInitializer, accessManager, this, object);
            return createSecureEntity(mapping.<T>getEntityType(), interceptor, decorator);
        }
    }

    boolean containsUnsecureObject(Object secureObject) {
        if (secureObject == null) {
            return true;
        }
        if (secureObject instanceof EntityProxy) {
            secureObject = ((EntityProxy)secureObject).getEntity();
        }
        return secureObject instanceof SecureObject;
    }

    <T> T getUnsecureObject(T secureObject) {
        if (secureObject == null) {
            return null;
        }
        if (secureObject instanceof EntityProxy) {
            secureObject = ((EntityProxy)secureObject).<T>getEntity();
        }
        if (secureObject instanceof SecureEntity) {
            SecureEntityProxyFactory proxyFactory = configuration.getSecureEntityProxyFactory();
            SecureEntityInterceptor secureEntityInterceptor
                = (SecureEntityInterceptor)proxyFactory.getInterceptor((SecureEntity)secureObject);
            return (T)secureEntityInterceptor.entity;
        } else if (secureObject instanceof AbstractSecureCollection) {
            return (T)((AbstractSecureCollection<?, Collection<?>>)secureObject).getOriginal();
        } else if (secureObject instanceof SecureList) {
            return (T)((SecureList<?>)secureObject).getOriginal();
        } else if (secureObject instanceof DefaultSecureMap) {
            return (T)((DefaultSecureMap<?, ?>)secureObject).getOriginal();
        } else {
            return createUnsecureObject(secureObject);
        }
    }

    abstract <T> T createUnsecureObject(T secureObject);

    void secureCopy(Object unsecureObject, Object secureObject) {
        ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(unsecureObject);
            if (propertyMapping.isRelationshipMapping()) {
                value = getSecureObject(value);
            }
            propertyMapping.setPropertyValue(secureObject, value);
        }
    }

    void unsecureCopy(final AccessType accessType, final Object secureObject, final Object unsecureObject) {
        if (secureObject instanceof SecureObject && !((SecureObject)secureObject).isInitialized()) {
            return;
        }
        boolean modified = false;
        final ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isIdProperty() || propertyMapping.isVersionProperty()) {
                continue; //don't change id or version property
            }
            Object secureValue = propertyMapping.getPropertyValue(secureObject);
            if (secureValue instanceof SecureCollection) {
                modified = secureCopy(classMapping,
                                      propertyMapping,
                                      accessType,
                                      secureObject,
                                      unsecureObject,
                                      (SecureCollection<?>)secureValue,
                                      modified);
            } else if (secureValue instanceof SecureMap) {
                modified = secureCopy(classMapping,
                                      propertyMapping,
                                      accessType,
                                      secureObject,
                                      unsecureObject,
                                      (SecureMap<?, ?>)secureValue,
                                      modified);
            } else {
                Object newValue;
                if (secureValue instanceof Collection) {
                    newValue = createUnsecureCollection((Collection<?>)secureValue);
                } else if (secureValue instanceof Map) {
                    newValue = createUnsecureMap((Map<?, ?>)secureValue);
                } else if (propertyMapping.isRelationshipMapping()) {
                    newValue = getUnsecureObject(secureValue);
                } else {
                    newValue = secureValue;
                }
                Object oldValue = propertyMapping.getPropertyValue(unsecureObject);
                if (isDirty(propertyMapping, newValue, oldValue)) {
                    if (!modified) {
                        checkAccess(accessType, secureObject);
                        fireLifecycleEvent(accessType, classMapping, secureObject);
                    }
                    propertyMapping.setPropertyValue(unsecureObject, newValue);
                    modified = true;
                }
            }
        }
    }

    private <K, V> Map<K, V> createUnsecureMap(Map<K, V> secureValue) {
        Map<K, V> unsecureMap = new HashMap<K, V>();
        for (Map.Entry<K, V> entry: secureValue.entrySet()) {
            K key = entry.getKey();
            if (!isSimplePropertyType(key.getClass())) {
                key = getUnsecureObject(key);
            }
            unsecureMap.put(key, getUnsecureObject(entry.getValue()));
        }
        return unsecureMap;
    }

    private <E, T extends List<E>> SecureList<E> createSecureList(T list,
                                                                  AbstractSecureObjectManager objectManager,
                                                                  AccessManager accessManager) {
        return new SecureList<E>(list, objectManager, accessManager);
    }

    private <E, T extends SortedSet<E>> SecureSortedSet<E> createSecureSortedSet(T sortedSet,
                                                                                 AbstractSecureObjectManager manager,
                                                                                 AccessManager accessManager) {
        return new SecureSortedSet<E>(sortedSet, manager, accessManager);
    }

    private <E, T extends Set<E>> SecureSet<E> createSecureSet(T set,
                                                               AbstractSecureObjectManager objectManager,
                                                               AccessManager accessManager) {
        return new SecureSet<E>(set, objectManager, accessManager);
    }

    private <E, T extends Collection<E>> SecureCollection<E> createSecureCollection(T collection,
                                                                                    AbstractSecureObjectManager mgr,
                                                                                    AccessManager accessManager) {
        return new DefaultSecureCollection<E, T>(collection, mgr, accessManager);
    }

    private <K, V> SecureMap<K, V> createSecureMap(Map<K, V> original,
                                                   AbstractSecureObjectManager objectManager,
                                                   AccessManager accessManager) {
        return new DefaultSecureMap<K, V>(original, objectManager, accessManager);
    }

    private <T> Collection<T> createUnsecureCollection(Collection<T> secureValue) {
        Collection<T> unsecureCollection = createCollection(secureValue);
        for (T entry: secureValue) {
            unsecureCollection.add(getUnsecureObject(entry));
        }
        return unsecureCollection;
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    <V> boolean secureCopy(ClassMappingInformation classMapping,
                           PropertyMappingInformation propertyMapping,
                           AccessType accessType,
                           Object secureOwner,
                           Object unsecureOwner,
                           SecureCollection<V> secureValue,
                           boolean modified) {
        Collection<V> unsecureCollection = getUnsecureObject(secureValue);
        SecureCollection<V> secureCollection = (SecureCollection<V>)getSecureObject(unsecureCollection);
        if (secureValue != secureCollection && secureValue.isDirty()) {
            secureCollection = secureValue.merge(secureCollection);
        }
        if (secureCollection.isDirty()) {
            if (!modified) {
                checkAccess(accessType, secureOwner);
                fireLifecycleEvent(accessType, classMapping, secureOwner);
            }
            if (accessType == AccessType.UPDATE) {
                if (secureCollection instanceof AbstractSecureCollection) {
                    ((AbstractSecureCollection<?, ?>)secureCollection).flush();
                } else if (secureCollection instanceof SecureList) {
                    ((SecureList<V>)secureCollection).flush();
                } else {
                    throw new IllegalStateException("unsupported secure collection type: " + secureCollection.getClass());
                }
            }
            modified = true;
        }
        if (secureCollection != secureValue) {
            propertyMapping.setPropertyValue(unsecureOwner, getUnsecureObject(secureCollection));
        }
        return modified;
    }

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    <K, V> boolean secureCopy(ClassMappingInformation classMapping,
                              PropertyMappingInformation propertyMapping,
                              AccessType accessType,
                              Object secureOwner,
                              Object unsecureOwner,
                              SecureMap<K, V> secureValue,
                              boolean modified) {
        Map<K, V> unsecureMap = getUnsecureObject(secureValue);
        SecureMap<K, V> secureMap = (SecureMap<K, V>)getSecureObject(unsecureMap);
        if (secureValue != secureMap && secureValue.isDirty()) {
            secureMap = secureValue.merge(secureMap);
        }
        if (secureMap.isDirty()) {
            if (!modified) {
                checkAccess(accessType, secureOwner);
                fireLifecycleEvent(accessType, classMapping, secureOwner);
            }
            if (accessType == AccessType.UPDATE) {
                if (secureMap instanceof DefaultSecureMap) {
                    ((DefaultSecureMap<K, V>)secureMap).flush();
                } else {
                    throw new IllegalStateException("unsupported secure map type: " + secureMap.getClass());
                }
            }
            modified = true;
        }
        if (secureMap != secureValue) {
            propertyMapping.setPropertyValue(unsecureOwner, getUnsecureObject(secureMap));
        }
        return modified;
    }

    void copyIdAndVersion(Object unsecureObject, Object secureObject) {
        if (secureObject instanceof EntityProxy) {
            secureObject = ((EntityProxy)secureObject).getEntity();
        }
        if (secureObject instanceof SecureObject && !((SecureObject)secureObject).isInitialized()) {
            return;
        }
        ClassMappingInformation classMapping = getClassMapping(secureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getIdPropertyMappings()) {
            Object newId = propertyMapping.getPropertyValue(unsecureObject);
            propertyMapping.setPropertyValue(secureObject, newId);
        }
        for (PropertyMappingInformation propertyMapping: classMapping.getVersionPropertyMappings()) {
            Object newVersion = propertyMapping.getPropertyValue(unsecureObject);
            propertyMapping.setPropertyValue(secureObject, newVersion);
        }
    }

    abstract void cascade(Object secureEntity,
                          Object unsecureEntity,
                          CascadeType cascadeType,
                          Set<SystemMapKey> alreadyCascadedEntities);

    boolean isDirty(Object newEntity, Object oldEntity) {
        final ClassMappingInformation classMapping = getClassMapping(newEntity.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isIdProperty() || propertyMapping.isVersionProperty()) {
                continue; //don't change id or version property
            }
            Object newValue = propertyMapping.getPropertyValue(newEntity);
            Object oldValue = propertyMapping.getPropertyValue(oldEntity);
            if (isDirty(propertyMapping, newValue, oldValue)) {
                return true;
            }
        }
        return false;
    }

    boolean isDirty(PropertyMappingInformation propertyMapping, Object newValue, Object oldValue) {
        if (newValue == oldValue) {
            return false;
        }
        if (!propertyMapping.isRelationshipMapping()) {
            return !nullSaveEquals(newValue, oldValue);
        } else if (propertyMapping.isSingleValued()) {
            return true; //because newValue != oldValue
        } else {
            CollectionValuedRelationshipMappingInformation relationshipMapping
                = (CollectionValuedRelationshipMappingInformation)propertyMapping;
            if (Collection.class.isAssignableFrom(relationshipMapping.getCollectionType())) {
                Collection<?> oldCollection = (Collection<?>)oldValue;
                Collection<?> newCollection = (Collection<?>)newValue;
                return oldCollection == null
                    || newCollection == null
                    || oldCollection.size() != newCollection.size()
                    || !oldCollection.containsAll(newCollection)
                    || !newCollection.containsAll(oldCollection);
            } else if (Map.class.isAssignableFrom(relationshipMapping.getCollectionType())) {
                Map<?, ?> oldMap = (Map<?, ?>)oldValue;
                Map<?, ?> newMap = (Map<?, ?>)newValue;
                return oldMap == null
                    || newMap == null
                    || oldMap.size() != newMap.size()
                    || !oldMap.entrySet().containsAll(newMap.entrySet())
                    || !newMap.entrySet().containsAll(oldMap.entrySet());
            } else {
                ExceptionFactory exceptionFactory = configuration.getExceptionFactory();
                throw exceptionFactory.createTypeNotFoundException(relationshipMapping.getCollectionType());
            }
        }
    }

    ClassMappingInformation getClassMapping(Class<?> type) {
        return mappingInformation.getClassMapping(type);
    }

    boolean isAccessible(AccessType accessType, Object entity) {
        return accessManager.isAccessible(accessType, entity);
    }

    void checkAccess(AccessType accessType, Object entity) {
        if (!accessManager.isAccessible(accessType, entity)) {
            throw new SecurityException("The current user is not permitted to " + accessType.toString().toLowerCase() + " the specified object of type " + getClassMapping(entity.getClass()).getEntityType().getName());
        }
    }

    void fireLifecycleEvent(AccessType accessType, ClassMappingInformation classMapping, Object entity) {
        switch (accessType) {
            case CREATE:
                firePersist(classMapping, entity);
                break;
            case UPDATE:
                fireUpdate(classMapping, entity);
                break;
            case DELETE:
                fireRemove(classMapping, entity);
                break;
            default:
                throw new IllegalArgumentException("unsupported accessType " + accessType);
        }
    }

    void firePersist(final ClassMappingInformation classMapping, final Object entity) {
        classMapping.prePersist(entity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postPersist(entity);
            }
        });
    }

    void fireUpdate(final ClassMappingInformation classMapping, final Object entity) {
        classMapping.preUpdate(entity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postUpdate(entity);
            }
        });
    }

    void fireRemove(final ClassMappingInformation classMapping, final Object entity) {
        classMapping.preRemove(entity);
        addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postRemove(entity);
            }
        });
    }

    <E> E createSecureEntity(Class<E> type, MethodInterceptor interceptor, Decorator<SecureEntity> decorator) {
        return (E)configuration.getSecureEntityProxyFactory().createSecureEntityProxy(type, interceptor, decorator);
    }

    void setRemoved(SecureEntity secureEntity) {
        SecureEntityDecorator secureEntityDecorator
            = (SecureEntityDecorator)configuration.getSecureEntityProxyFactory().getDecorator(secureEntity);
        secureEntityDecorator.deleted = true;
    }

    void initialize(SecureEntity secureEntity, boolean checkAccess) {
        SecureEntityDecorator secureEntityDecorator
            = (SecureEntityDecorator)configuration.getSecureEntityProxyFactory().getDecorator(secureEntity);
        secureEntityDecorator.refresh(checkAccess);
    }

    private <T> Collection<T> createCollection(Collection<T> original) {
        if (original instanceof SortedSet) {
            return new TreeSet<T>(((SortedSet<T>)original).comparator());
        } else if (original instanceof Set) {
            return new LinkedHashSet<T>();
        } else {
            return new ArrayList<T>();
        }
    }

    private boolean nullSaveEquals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null) {
            return false;
        }
        return object1.equals(object2);
    }
}
