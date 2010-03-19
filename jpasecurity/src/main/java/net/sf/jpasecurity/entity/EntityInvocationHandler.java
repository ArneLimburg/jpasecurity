/*
 * Copyright 2008 - 2009 Arne Limburg
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SuperMethod;
import net.sf.jpasecurity.util.AbstractInvocationHandler;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements SecureEntity, MethodInterceptor {

    private MappingInformation mapping;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private boolean initialized;
    private boolean deleted;
    private SecureEntity secureEntity;
    Object entity;
    private boolean isTransient;
    private transient ThreadLocal<Boolean> updating;

    public EntityInvocationHandler(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity) {
        this(mapping, accessManager, objectManager, entity, false);
    }

    public EntityInvocationHandler(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity,
                                   boolean isTransient) {
        this.mapping = mapping;
        this.accessManager = accessManager;
        this.objectManager = objectManager;
        this.entity = entity;
        this.isTransient = isTransient;
    }

    public SecureEntity createSecureEntity() {
        return objectManager.createSecureEntity(mapping.getClassMapping(entity.getClass()).getEntityType(), this);
    }

    public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args) throws Throwable {
        if (secureEntity == null) {
            if (!(object instanceof SecureEntity)) {
                throw new IllegalStateException("intercepted object must be of type SecureEntity");
            }
            secureEntity = (SecureEntity)object;
        }
        if (object != secureEntity) {
            throw new IllegalStateException("unexpected object " + object + ", expected " + secureEntity);
        }
        if (!isInitialized() && !isUpdating()) {
            initialize();
            mapping.getClassMapping(entity.getClass()).postLoad(secureEntity);
        }
        if (canInvoke(method)) {
            return invoke(object, method, args);
        }
        if (isHashCode(method)) {
            return entity.hashCode();
        } else if (isEquals(method)) {
            Object value = args[0];
            if (objectManager.isSecureObject(value)) {
                value = objectManager.getUnsecureObject(value);
            }
            return entity.equals(value);
        } else if (isToString(method)) {
            return entity.toString();
        }
        try {
            return superMethod.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAccessible(AccessType accessType) {
        return accessManager.isAccessible(accessType, entity);
    }

    /**
     * Returns <tt>true</tt>, if the entity of this invocation handler
     * is contained in the specified <tt>EntityManager</tt>.
     * @param entityManager the entity manager
     * @return <tt>true</tt>, if the entity of this invocation handler
     *         is contained in the specified <tt>EntityManager</tt>, <tt>false</tt> otherwise
     */
    public boolean isContained(EntityManager entityManager) {
        return entityManager.contains(entity);
    }

    public boolean isRemoved() {
        return deleted;
    }

    /**
     * @throws SecurityException when the current user is not allowed to merge the entity of this invocation handler
     */
    public Object merge(EntityManager entityManager, SecureObjectManager objectManager) {
        checkAccess(entity, AccessType.UPDATE, CascadeType.MERGE, new HashSet<Object>());
        Object mergedEntity = entityManager.merge(entity);
        return objectManager.getSecureObject(mergedEntity);
    }

    /**
     * @throws SecurityException when the current user is not allowed to remove the entity of this invocation handler
     */
    public void remove(EntityManager entityManager) {
        try {
            checkAccess(entity, AccessType.DELETE, CascadeType.REMOVE, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        final ClassMappingInformation classMapping = mapping.getClassMapping(secureEntity.getClass());
        classMapping.preRemove(secureEntity);
        objectManager.addPostFlushOperation(new Runnable() {
            public void run() {
                classMapping.postRemove(secureEntity);
            }
        });
        entityManager.remove(entity);
        deleted = true;
    }

    /**
     * @throws SecurityException when the current user is not allowed to refresh the entity of this invocation handler
     */
    public void refresh(EntityManager entityManager) {
        try {
            checkAccess(entity, AccessType.READ, CascadeType.REFRESH, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        entityManager.refresh(entity);
        initialized = false;
    }

    /**
     * @throws SecurityException when the current user is not allowed to lock the entity of this invocation handler
     */
    public void lock(EntityManager entityManager, LockModeType lockMode) {
        if (lockMode == LockModeType.READ && !accessManager.isAccessible(AccessType.READ, entity)) {
            throw new SecurityException();
        } else if (lockMode == LockModeType.WRITE && !accessManager.isAccessible(AccessType.UPDATE, entity)) {
            throw new SecurityException();
        }
        entityManager.lock(entity, lockMode);
    }

    public Query setParameter(Query query, int index) {
        return query.setParameter(index, entity);
    }

    public Query setParameter(Query query, String name) {
        return query.setParameter(name, entity);
    }

    public void flush() {
        if (!isReadOnly()) {
            objectManager.unsecureCopy(AccessType.UPDATE, secureEntity, entity);
            objectManager.copyIdAndVersion(entity, secureEntity);
        }
    }

    private void checkAccess(Object object,
                             AccessType accessType,
                             CascadeType cascadeType,
                             Set<Object> checkedEntities) {
        if (checkedEntities.contains(object) || !isInitialized(object)) {
            return;
        }
        if (object instanceof Collection) {
            checkAccess((Collection)object, accessType, cascadeType, checkedEntities);
        } else {
            if (!accessManager.isAccessible(accessType, object)) {
                throw new SecurityException("The current user is not permitted to access the specified object");
            }
            checkedEntities.add(object);
            if (cascadeType != null) {
                ClassMappingInformation classMapping = mapping.getClassMapping(object.getClass());
                for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
                    if (propertyMapping.getCascadeTypes().contains(cascadeType)
                            || propertyMapping.getCascadeTypes().contains(CascadeType.ALL)) {
                        try {
                            Object value = propertyMapping.getPropertyValue(object);
                            if (value != null) {
                                checkAccess(value, accessType, cascadeType, checkedEntities);
                            }
                        } catch (PropertyAccessException e) {
                            throw new PropertyAccessException(propertyMapping.getPropertyName() + "." + e.getPropertyName());
                        } catch (SecurityException e) {
                            throw new PropertyAccessException(propertyMapping.getPropertyName());
                        }
                    }
                }
            }
        }
    }

    private void checkAccess(Collection<?> collection,
                             AccessType accessType,
                             CascadeType cascadeType,
                             Set<Object> checkedEntities) {
        for (Object object: collection) {
            checkAccess(object, accessType, cascadeType, checkedEntities);
        }
    }

    public boolean isReadOnly() {
        return isTransient;
    }

    void initialize() {
        try {
            setUpdating(true);
            checkAccess(entity, AccessType.READ, null, new HashSet<Object>());
            entity = unproxy(entity);
            objectManager.secureCopy(entity, secureEntity);
            initialized = true;
        } finally {
            setUpdating(false);
        }
    }

    private boolean isUpdating() {
        return updating != null && updating.get() != null && updating.get();
    }

    private void setUpdating(boolean isUpdating) {
        if (updating == null) {
            updating = new ThreadLocal<Boolean>();
        }
        if (isUpdating) {
            updating.set(true);
        } else {
            updating.remove();
        }
    }

    private boolean isInitialized(Object entity) {
        return !(entity instanceof SecureEntity) || ((SecureEntity)entity).isInitialized();
    }

    private Object unproxy(Object object) {
        try {
            Class<?> hibernateProxy
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            if (!hibernateProxy.isInstance(object)) {
                return object;
            }
            Class<?> lazyInitializer
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer");
            Object lazyInitializerInstance = hibernateProxy.getMethod("getHibernateLazyInitializer").invoke(object);
            return lazyInitializer.getMethod("getImplementation").invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }
}
