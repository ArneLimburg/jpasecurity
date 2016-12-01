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
package org.jpasecurity.model;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 * @author Arne Limburg
 */
public class TestEntityListener {

    @PrePersist
    public void publicTestMethod(Object entity) {
        if (entity == null) {
            throw new PublicTestMethodCalledException();
        }
    }

    @PreRemove
    protected void protectedTestMethod(Object entity) {
        if (entity == null) {
            throw new ProtectedTestMethodCalledException();
        }
    }

    @PreUpdate
    void packageProtectedTestMethod(Object entity) {
        if (entity == null) {
            throw new PackageProtectedTestMethodCalledException();
        }
    }

    @SuppressWarnings("unused") // is used by reflection
    private void privateTestMethod(Object entity) {
        if (entity == null) {
            throw new PrivateTestMethodCalledException();
        }
    }

    public static class PublicTestMethodCalledException extends RuntimeException {
    }

    public static class ProtectedTestMethodCalledException extends RuntimeException {
    }

    public static class PackageProtectedTestMethodCalledException extends RuntimeException {
    }

    public static class PrivateTestMethodCalledException extends RuntimeException {
    }
}
