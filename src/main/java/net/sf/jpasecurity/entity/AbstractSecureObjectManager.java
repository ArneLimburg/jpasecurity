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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;

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
        boolean modified = false;
        final ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isIdProperty() || propertyMapping.isVersionProperty()) {
                continue; //don't change id or version property
            }
            Object secureValue = propertyMapping.getPropertyValue(secureObject);
            if (secureValue instanceof SecureCollection) {
                Object unsecureCollection = getUnsecureObject(secureValue);
                SecureCollection<Object> secureCollection
                    = (SecureCollection<Object>)getSecureObject(unsecureCollection);
                if (secureValue != secureCollection && ((SecureCollection<?>)secureValue).isDirty()) {
                    secureCollection = ((SecureCollection<Object>)secureValue).merge(secureCollection);
                }
                if (secureCollection.isDirty()) {
                    if (!modified) {
                        checkAccess(accessType, secureObject);
                        fireLifecycleEvent(accessType, classMapping, secureObject);
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
                    propertyMapping.setPropertyValue(unsecureObject, getUnsecureObject(secureCollection));
                }
                continue;
            }
            Object newValue = secureValue;
            if (secureValue instanceof Collection) {
                Collection<Object> unsecureCollection = createCollection(secureValue);
                for (Object entry: (Collection<?>)secureValue) {
                    unsecureCollection.add(getUnsecureObject(entry));
                }
                newValue = unsecureCollection;
            } else if (propertyMapping.isRelationshipMapping()) {
                newValue = getUnsecureObject(secureValue);
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

    void copyIdAndVersion(Object unsecureObject, Object secureObject) {
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
                          Set<Object> alreadyCascadedEntities);

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
        if (propertyMapping instanceof CollectionValuedRelationshipMappingInformation) {
            Collection<?> oldCollection = (Collection<?>)oldValue;
            Collection<?> newCollection = (Collection<?>)newValue;
            return oldCollection == null
                || newCollection == null
                || oldCollection.size() != newCollection.size()
                || !oldCollection.containsAll(newCollection)
                || !newCollection.containsAll(oldCollection);
        } else if (propertyMapping.isRelationshipMapping()) {
            return true; //because newValue != oldValue
        } else {
            return !nullSaveEquals(newValue, oldValue);
        }
    }

    ClassMappingInformation getClassMapping(Class<?> type) {
        return mappingInformation.getClassMapping(type);
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
