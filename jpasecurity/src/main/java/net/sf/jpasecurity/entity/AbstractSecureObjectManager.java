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

import javax.persistence.CascadeType;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.SecureObject;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.proxy.EntityProxy;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.util.SystemMapKey;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectManager implements SecureObjectManager {

    private MappingInformation mappingInformation;
    private AccessManager accessManager;
    private SecureEntityProxyFactory proxyFactory;
    private final List<Runnable> postFlushOperations = new ArrayList<Runnable>();

    public AbstractSecureObjectManager(MappingInformation mappingInformation,
                                       AccessManager accessManager,
                                       SecureEntityProxyFactory proxyFactory) {
        this.mappingInformation = mappingInformation;
        this.accessManager = accessManager;
        this.proxyFactory = proxyFactory;
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
            return (T)new SecureList((List<?>)object, this, accessManager);
        } else if (object instanceof SortedSet) {
            return (T)new SecureSortedSet((SortedSet<?>)object, this, accessManager);
        } else if (object instanceof Set) {
            return (T)new SecureSet((Set<?>)object, this, accessManager);
        } else if (object instanceof Collection) {
            return (T)new DefaultSecureCollection((Collection<?>)object, this, accessManager);
        } else if (object instanceof Map) {
            return (T)new DefaultSecureMap<Object, Object>((Map<Object, Object>)object, this, accessManager);
        } else {
            EntityInvocationHandler entityInvocationHandler
                = new EntityInvocationHandler(mappingInformation, accessManager, this, object);
            return (T)entityInvocationHandler.createSecureEntity();
        }
    }

    <T> T getUnsecureObject(T secureObject) {
        if (secureObject == null) {
            return null;
        }
        if (secureObject instanceof EntityProxy) {
            secureObject = (T)((EntityProxy)secureObject).getEntity();
        }
        if (secureObject instanceof SecureEntity) {
            EntityInvocationHandler entityInvocationHandler
                = (EntityInvocationHandler)proxyFactory.getMethodInterceptor((SecureEntity)secureObject);
            return (T)entityInvocationHandler.entity;
        } else if (secureObject instanceof AbstractSecureCollection) {
            return (T)((AbstractSecureCollection<?, Collection<?>>)secureObject).getOriginal();
        } else if (secureObject instanceof SecureList) {
            return (T)((SecureList<?>)secureObject).getOriginal();
        } else if (secureObject instanceof DefaultSecureMap) {
            return (T)((DefaultSecureMap<?, ?>)secureObject).getOriginal();
        } else {
            //TODO bigsteff review
            if (mappingInformation.getClassMapping(secureObject.getClass()) == null) {
               return secureObject;
            }
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
                                      (SecureCollection<Object>)secureValue,
                                      modified);
            } else if (secureValue instanceof SecureMap) {
                modified = secureCopy(classMapping,
                                      propertyMapping,
                                      accessType,
                                      secureObject,
                                      unsecureObject,
                                      (SecureMap<Object, Object>)secureValue,
                                      modified);
            } else {
                Object newValue;
                if (secureValue instanceof Collection) {
                    Collection<Object> unsecureCollection = createCollection(secureValue);
                    for (Object entry: (Collection<?>)secureValue) {
                        unsecureCollection.add(getUnsecureObject(entry));
                    }
                    newValue = unsecureCollection;
                } else if (secureValue instanceof Map) {
                    Map<Object, Object> unsecureMap = new HashMap<Object, Object>();
                    for (Map.Entry<Object, Object> entry: ((Map<Object, Object>)secureValue).entrySet()) {
                        Object key = entry.getKey();
                        if (!isSimplePropertyType(key.getClass())) {
                            key = getUnsecureObject(key);
                        }
                        unsecureMap.put(key, getUnsecureObject(entry.getValue()));
                    }
                    newValue = unsecureMap;
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

    /**
     * @return <tt>true</tt>, if the owner is modified, <tt>false</tt> otherwise.
     */
    boolean secureCopy(ClassMappingInformation classMapping,
                       PropertyMappingInformation propertyMapping,
                       AccessType accessType,
                       Object secureOwner,
                       Object unsecureOwner,
                       SecureCollection<Object> secureValue,
                       boolean modified) {
        Object unsecureCollection = getUnsecureObject(secureValue);
        SecureCollection<Object> secureCollection
            = (SecureCollection<Object>)getSecureObject(unsecureCollection);
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
                    ((AbstractSecureCollection<?, Collection<?>>)secureCollection).flush();
                } else if (secureCollection instanceof SecureList) {
                    ((SecureList<?>)secureCollection).flush();
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
    boolean secureCopy(ClassMappingInformation classMapping,
                       PropertyMappingInformation propertyMapping,
                       AccessType accessType,
                       Object secureOwner,
                       Object unsecureOwner,
                       SecureMap<Object, Object> secureValue,
                       boolean modified) {
        Object unsecureMap = getUnsecureObject(secureValue);
        SecureMap<Object, Object> secureMap
            = (SecureMap<Object, Object>)getSecureObject(unsecureMap);
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
                    ((DefaultSecureMap<Object, Object>)secureMap).flush();
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
        ClassMappingInformation classMapping = getClassMapping(secureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getIdPropertyMappings()) {
            Object newId = propertyMapping.getPropertyValue(unsecureObject);
            //TODO bigsteff review
            if (secureObject instanceof EntityProxy) {
               propertyMapping.setPropertyValue(((EntityProxy)secureObject).getEntity(), newId);
            }
            propertyMapping.setPropertyValue(secureObject, newId);
        }
        for (PropertyMappingInformation propertyMapping: classMapping.getVersionPropertyMappings()) {
            Object newVersion = propertyMapping.getPropertyValue(unsecureObject);
            //TODO bigsteff review
            if (secureObject instanceof EntityProxy) {
                propertyMapping.setPropertyValue(((EntityProxy)secureObject).getEntity(), newVersion);
            }
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
                throw new PersistenceException("unsupported to-many-type " + relationshipMapping.getCollectionType().getName());
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

    SecureEntity createSecureEntity(Class<?> type, MethodInterceptor interceptor) {
        return proxyFactory.createSecureEntityProxy(type, interceptor);
    }

    void setRemoved(SecureEntity secureEntity) {
        EntityInvocationHandler entityInvocationHandler
            = (EntityInvocationHandler)proxyFactory.getMethodInterceptor(secureEntity);
        entityInvocationHandler.deleted = true;
    }

    boolean isInitialized(SecureEntity secureEntity) {
        EntityInvocationHandler entityInvocationHandler
            = (EntityInvocationHandler)proxyFactory.getMethodInterceptor(secureEntity);
        return entityInvocationHandler.isInitialized();
    }

    void initialize(SecureEntity secureEntity) {
        EntityInvocationHandler entityInvocationHandler
            = (EntityInvocationHandler)proxyFactory.getMethodInterceptor(secureEntity);
        entityInvocationHandler.refresh();
    }

    private Collection<Object> createCollection(Object original) {
        if (original instanceof SortedSet) {
            return new TreeSet<Object>(((SortedSet<Object>)original).comparator());
        } else if (original instanceof Set) {
            return new LinkedHashSet<Object>();
        } else {
            return new ArrayList<Object>();
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
