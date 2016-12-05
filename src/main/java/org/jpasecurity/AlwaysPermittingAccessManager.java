/*
 * Copyright 2011 Arne Limburg
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

import java.util.Collection;

/**
 * An implementation of the {@link AccessManager} that always returns <tt>true</tt>.
 * @author Arne Limburg
 */
public class AlwaysPermittingAccessManager implements AccessManager {

    public boolean isAccessible(AccessType accessType, String entityName, Object... constructorArgs) {
        return true;
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        return true;
    }

    @Override
    public void checkAccess(AccessType accessType, Object entity) {
    }

    @Override
    public void delayChecks() {
    }

    @Override
    public void checkNow() {
    }

    @Override
    public void disableChecks() {
    }

    @Override
    public void enableChecks() {
    }

    @Override
    public void ignoreChecks(AccessType accessType, Collection<?> entities) {
    }
}
