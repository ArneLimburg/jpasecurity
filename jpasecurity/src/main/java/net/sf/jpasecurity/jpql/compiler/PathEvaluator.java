/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import java.util.Collection;
import java.util.List;

/**
 * @author Arne Limburg
 */
public interface PathEvaluator {

    /**
     * Evaluates a single-valued property path.
     * @param root the object to evaluate the path on
     * @param path the property path
     * @return the result
     * @throws javax.persistence.PersistenceException if the property path is not single-valued
     */
    Object evaluate(Object root, String path);

    /**
     * Evaluates a property path.
     * @param root the object to evaluate the path on
     * @param path the property path
     * @return the result
     * @throws javax.persistence.PersistenceException if the property path is not single-valued
     */
    List<Object> evaluateAll(Collection<?> root, String path);
}
