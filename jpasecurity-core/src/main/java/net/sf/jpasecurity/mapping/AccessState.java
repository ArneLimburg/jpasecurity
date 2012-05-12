/*
 * Copyright 2012 Raffaela Ferrari
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


/**
 *
 * @author Raffaela Ferrari
 *
 */
public enum AccessState {
    NO_ACCESS_DEFINED(null, null),
    FIELD_ACCESS_FOR_HIERARCHY(true, false),
    PROPERTY_ACCESS_FOR_HIERARCHY(false, false),
    FIELD_ACCESS(true, null),
    PROPERTY_ACCESS(false, null),
    OVERRIDING_PROPERTY_ACCESS(false, true),
    OVERRIDING_FIELD_ACCESS(true, true);

    private Boolean fieldAccess;
    private Boolean overriding;

    private AccessState(Boolean fieldAccess, Boolean overriding) {
        this.fieldAccess = fieldAccess;
        this.overriding = overriding;
    }

    public boolean isFieldAccess() {
        return fieldAccess != null && fieldAccess;
    }

    public boolean isPropertyAccess() {
        return fieldAccess != null && !fieldAccess;
    }

    public boolean isOverriding() {
        return overriding != null && overriding;
    }

    public boolean isDefiningHierarchy() {
        return overriding != null && !overriding;
    }
}
