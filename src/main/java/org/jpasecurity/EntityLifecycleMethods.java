/*
 * Copyright 2010 - 2016 Arne Limburg
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
package org.jpasecurity;

import java.lang.reflect.Method;

/**
 * This method serves as a value holder for entity lifecycle-methods.
 * @author Arne Limburg
 */
public class EntityLifecycleMethods {

    private Method prePersistMethod = null;
    private Method postPersistMethod = null;
    private Method preRemoveMethod = null;
    private Method postRemoveMethod = null;
    private Method preUpdateMethod = null;
    private Method postUpdateMethod = null;
    private Method postLoadMethod = null;

    public Method getPrePersistMethod() {
        return prePersistMethod;
    }

    public void setPrePersistMethod(Method prePersistMethod) {
        this.prePersistMethod = prePersistMethod;
    }

    public Method getPostPersistMethod() {
        return postPersistMethod;
    }

    public void setPostPersistMethod(Method postPersistMethod) {
        this.postPersistMethod = postPersistMethod;
    }

    public Method getPreRemoveMethod() {
        return preRemoveMethod;
    }

    public void setPreRemoveMethod(Method preRemoveMethod) {
        this.preRemoveMethod = preRemoveMethod;
    }

    public Method getPostRemoveMethod() {
        return postRemoveMethod;
    }

    public void setPostRemoveMethod(Method postRemoveMethod) {
        this.postRemoveMethod = postRemoveMethod;
    }

    public Method getPreUpdateMethod() {
        return preUpdateMethod;
    }

    public void setPreUpdateMethod(Method preUpdateMethod) {
        this.preUpdateMethod = preUpdateMethod;
    }

    public Method getPostUpdateMethod() {
        return postUpdateMethod;
    }

    public void setPostUpdateMethod(Method postUpdateMethod) {
        this.postUpdateMethod = postUpdateMethod;
    }

    public Method getPostLoadMethod() {
        return postLoadMethod;
    }

    public void setPostLoadMethod(Method postLoadMethod) {
        this.postLoadMethod = postLoadMethod;
    }
}
