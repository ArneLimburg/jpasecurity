/*
 * Copyright 2008 - 2011 Arne Limburg
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
package org.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.PropertyMappingInformation;

/**
 * @author Arne Limburg
 */
public class MappedPathEvaluator implements PathEvaluator {

    private MappingInformation mappingInformation;
    private ExceptionFactory exceptionFactory;

    public MappedPathEvaluator(MappingInformation mappingInformation, ExceptionFactory exceptionFactory) {
        this.mappingInformation = mappingInformation;
        this.exceptionFactory = exceptionFactory;
    }

    public Object evaluate(Object root, String path) {
        if (root == null) {
            return null;
        }
        final Collection<?> rootCollection =
            root instanceof Collection ? (Collection<?>)root : Collections.singleton(root);
        Collection<?> result = evaluateAll(rootCollection, path);
        if (result.size() > 1) {
            throw exceptionFactory.createInvalidPathException(path, "path is not single-valued");
        }
        return result.isEmpty()? null: result.iterator().next();
    }

    public <R> List<R> evaluateAll(final Collection<?> root, String path) {
        String[] pathElements = path.split("\\.");
        List<Object> rootCollection = new ArrayList<Object>(root);
        List<R> resultCollection = new ArrayList<R>();
        for (String property: pathElements) {
            resultCollection.clear();
            for (Object rootObject: rootCollection) {
                if (rootObject == null) {
                    continue;
                }
                ClassMappingInformation classMapping = mappingInformation.getClassMapping(rootObject.getClass());
                if (classMapping.containsPropertyMapping(property)) {
                    PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(property);
                    Object result = propertyMapping.getPropertyValue(rootObject);
                    if (result instanceof Collection) {
                        resultCollection.addAll((Collection<R>)result);
                    } else if (result != null) {
                        resultCollection.add((R)result);
                    }
                } // else the property may be of a subclass and this path is ruled out by inner join on subclass table
            }
            rootCollection.clear();
            for (Object resultObject: resultCollection) {
                if (resultObject instanceof Collection) {
                    rootCollection.addAll((Collection<Object>)resultObject);
                } else {
                    rootCollection.add(resultObject);
                }
            }
        }
        return resultCollection;
    }
}
