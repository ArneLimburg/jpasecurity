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

/**
 * @author Arne Limburg
 */
public abstract class AbstractExceptionFactory implements ExceptionFactory {

    public RuntimeException createTypeNotFoundException(Class<?> type) {
        return createTypeNotFoundException(type.getName());
    }

    public RuntimeException createTypeNotFoundException(String className) {
        return createRuntimeException("Mapping not found for type " + className, null);
    }

    public RuntimeException createInvalidPathException(String path, String error) {
        return createRuntimeException("invalid path " + path + ": " + error, null);
    }

    public RuntimeException createRuntimeException(String message) {
        return createRuntimeException(message, null);
    }

    public RuntimeException createRuntimeException(Throwable cause) {
        return createRuntimeException(null, cause);
    }

    public abstract RuntimeException createRuntimeException(String errorMessage, Throwable cause);
}
