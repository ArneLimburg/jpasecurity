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

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.proxy.Decorator;
import net.sf.jpasecurity.util.AbstractInvocationHandler;

/**
 * A class to decorate a bean to become a {@link SecureEntity}.
 * @author Arne Limburg
 */
public class SecureEntityDecorator extends AbstractInvocationHandler implements SecureEntity, Decorator<SecureEntity> {

    private ClassMappingInformation mapping;
    private ObjectWrapper objectWrapper;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private boolean initialized;
    boolean deleted;
    private SecureEntity delegate;
    private Object entity;
    private boolean isTransient;
    private transient ThreadLocal<Boolean> updating;

    public SecureEntityDecorator(ClassMappingInformation mapping,
                                 ObjectWrapper objectWrapper,
                                 AccessManager accessManager,
                                 AbstractSecureObjectManager objectManager,
                                 Object entity) {
        this(mapping, objectWrapper, accessManager, objectManager, entity, false);
    }

    public SecureEntityDecorator(ClassMappingInformation mapping,
                                 ObjectWrapper objectWrapper,
                                 AccessManager accessManager,
                                 AbstractSecureObjectManager objectManager,
                                 Object entity,
                                 boolean isTransient) {
        this.mapping = mapping;
        this.objectWrapper = objectWrapper;
        this.accessManager = accessManager;
        this.objectManager = objectManager;
        this.entity = entity;
        this.isTransient = isTransient;
    }

    public void setDelegate(SecureEntity delegate) {
        this.delegate = delegate;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAccessible(AccessType accessType) {
        return accessManager.isAccessible(accessType, entity);
    }

    public boolean isRemoved() {
        return deleted;
    }

    public void flush() {
        if (!isReadOnly() && isInitialized()) {
            objectManager.unsecureCopy(AccessType.UPDATE, delegate, entity);
        }
    }

    public boolean isReadOnly() {
        return isTransient;
    }

    public void refresh() {
        refresh(true);
    }

    void refresh(boolean checkAccess) {
        if (isUpdating()) {
            return; //we are already refreshing
        }
        try {
            setUpdating(true);
            boolean oldInitialized = initialized;
            entity = objectWrapper.unwrap(entity);
            if (checkAccess && !accessManager.isAccessible(AccessType.READ, entity)) {
                throw new SecurityException("The current user is not permitted to access the specified object");
            }
            objectManager.secureCopy(entity, delegate);
            initialized = true;
            if (initialized != oldInitialized) {
                mapping.postLoad(delegate);
            }
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
}
