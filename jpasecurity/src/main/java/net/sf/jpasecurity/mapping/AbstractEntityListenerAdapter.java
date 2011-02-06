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
package net.sf.jpasecurity.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.jpasecurity.ExceptionFactory;

/**
 * This class is a base-class for {@link EntityListener}s.
 * @author Arne Limburg
 */
public abstract class AbstractEntityListenerAdapter implements EntityListener {

    private final EntityLifecycleMethods entityLifecycleMethods;
    private final ExceptionFactory exceptionFactory;

    public AbstractEntityListenerAdapter(EntityLifecycleMethods entityLifecycleMethods, ExceptionFactory factory) {
        this.entityLifecycleMethods = entityLifecycleMethods;
        this.exceptionFactory = factory;
    }

    public void prePersist(Object entity) {
        if (entityLifecycleMethods.getPrePersistMethod() != null) {
            invoke(entityLifecycleMethods.getPrePersistMethod(), entity);
        }
    }

    public void postPersist(Object entity) {
        if (entityLifecycleMethods.getPostPersistMethod() != null) {
            invoke(entityLifecycleMethods.getPostPersistMethod(), entity);
        }
    }

    public void preRemove(Object entity) {
        if (entityLifecycleMethods.getPreRemoveMethod() != null) {
            invoke(entityLifecycleMethods.getPreRemoveMethod(), entity);
        }
    }

    public void postRemove(Object entity) {
        if (entityLifecycleMethods.getPostRemoveMethod() != null) {
            invoke(entityLifecycleMethods.getPostRemoveMethod(), entity);
        }
    }

    public void preUpdate(Object entity) {
        if (entityLifecycleMethods.getPreUpdateMethod() != null) {
            invoke(entityLifecycleMethods.getPreUpdateMethod(), entity);
        }
    }

    public void postUpdate(Object entity) {
        if (entityLifecycleMethods.getPostUpdateMethod() != null) {
            invoke(entityLifecycleMethods.getPostUpdateMethod(), entity);
        }
    }

    public void postLoad(Object entity) {
        if (entityLifecycleMethods.getPostLoadMethod() != null) {
            invoke(entityLifecycleMethods.getPostLoadMethod(), entity);
        }
    }

    private void invoke(Method method, Object entity) {
        try {
            fireEvent(method, entity);
        } catch (IllegalAccessException e) {
            throw exceptionFactory.createRuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException)e.getTargetException();
            } else {
                throw exceptionFactory.createRuntimeException(e.getTargetException());
            }
        }
    }

    protected abstract void fireEvent(Method method, Object entity) throws IllegalAccessException,
                                                                           InvocationTargetException;
}
