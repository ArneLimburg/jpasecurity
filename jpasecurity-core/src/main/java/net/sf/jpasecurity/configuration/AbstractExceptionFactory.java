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
package net.sf.jpasecurity.configuration;

import java.lang.reflect.Member;

import net.sf.jpasecurity.ExceptionFactory;

/**
 * @author Arne Limburg
 */
public abstract class AbstractExceptionFactory implements ExceptionFactory {

    public RuntimeException createTargetEntityNotFoundException(Member property) {
        String error = "Could not determine target entity for property \"" + property.getName()
                     + "\" of class " + property.getDeclaringClass().getName();
        return createMappingException(error);
    }

    public RuntimeException createMappingException(String message) {
        return createRuntimeException(message);
    }

    public RuntimeException createTypeNotFoundException(Class<?> type) {
        return createTypeNotFoundException(type == null? null: type.getName());
    }

    public RuntimeException createTypeNotFoundException(String className) {
        return createRuntimeException("Mapping not found for type " + className);
    }

    public RuntimeException createTypeDefinitionNotFoundException(String alias) {
        return createRuntimeException("Type not found for alias \"" + alias + '"');
    }

    public RuntimeException createPropertyNotFoundException(Class<?> type, String propertyName) {
        String message = "property \"" + propertyName + "\" of class \"" + type.getName() + "\" is not mapped";
        return createRuntimeException(message);
    }

    public RuntimeException createInvalidPathException(String path, String error) {
        return createRuntimeException("invalid path " + path + ": " + error);
    }

    public RuntimeException createRuntimeException(String message) {
        return createRuntimeException(message, null);
    }

    public RuntimeException createRuntimeException(Throwable cause) {
        return createRuntimeException(null, cause);
    }

    public RuntimeException createMissingAliasException(String type) {
        return createRuntimeException("missing alias for type " + type);
    }

    public abstract RuntimeException createRuntimeException(String errorMessage, Throwable cause);
}
