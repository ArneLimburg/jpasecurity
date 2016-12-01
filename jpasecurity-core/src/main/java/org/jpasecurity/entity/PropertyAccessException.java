/*
 * Copyright 2009 Arne Limburg
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
package org.jpasecurity.entity;

/**
 * @author Arne Limburg
 */
public class PropertyAccessException extends SecurityException {

    private static final long serialVersionUID = -1570325395572179045L;

    private String propertyName;

    public PropertyAccessException(String propertyName) {
        super("The current user is not permitted to access property " + propertyName);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
