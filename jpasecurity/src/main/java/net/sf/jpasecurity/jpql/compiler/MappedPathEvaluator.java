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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;

/**
 * @author Arne Limburg
 */
public class MappedPathEvaluator implements PathEvaluator {

    private MappingInformation mappingInformation;

    public MappedPathEvaluator(MappingInformation mappingInformation) {
        this.mappingInformation = mappingInformation;
    }

    public Object evaluate(Object root, String path) {
        Collection<Object> result = evaluateAll(Collections.singleton(root), path);
        if (result.size() > 1) {
            throw new PersistenceException("path '" + path + "' is not single-valued");
        }
        return result.isEmpty()? null: result.iterator().next();
    }

    public Collection<Object> evaluateAll(Collection<Object> root, String path) {
        String[] pathElements = path.split("\\.");
        Collection rootCollection = new ArrayList<Object>(root);
        Collection<Object> resultCollection = new ArrayList<Object>();
        for (String property: pathElements) {
            resultCollection.clear();
            for (Object rootObject: rootCollection) {
                ClassMappingInformation classMapping = mappingInformation.getClassMapping(rootObject.getClass());
                if (classMapping == null) {
                    throw new PersistenceException("class '" + rootObject.getClass().getName() + "' is not mapped");
                }
                PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(property);
                if (propertyMapping == null) {
                    throw new PersistenceException("property '" + property + "' of class '" + rootObject.getClass().getName() + "' is not mapped");
                }
                resultCollection.add(propertyMapping.getPropertyValue(rootObject));
            }
            rootCollection.clear();
            for (Object resultObject: resultCollection) {
                if (resultObject instanceof Collection) {
                    rootCollection.addAll((Collection)resultObject);
                } else {
                    rootCollection.add(resultObject);
                }
            }
        }
        return resultCollection;
    }
}
