/*
 * Copyright 2008 - 2016 Arne Limburg
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

import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.access.SecurePersistenceUnitUtil;

/**
 * @author Arne Limburg
 */
public class MappedPathEvaluator implements PathEvaluator {

    private Metamodel metamodel;
    private SecurePersistenceUnitUtil persistenceUnitUtil;

    public MappedPathEvaluator(Metamodel metamodel, SecurePersistenceUnitUtil unitUtil) {
        this.metamodel = metamodel;
        this.persistenceUnitUtil = unitUtil;
    }

    public Object evaluate(Object root, String path) {
        if (root == null) {
            return null;
        }
        Collection<?> rootCollection =
            root instanceof Collection ? (Collection<?>)root : Collections.singleton(root);
        Collection<?> result = evaluateAll(rootCollection, path);
        if (result.size() > 1) {
            throw new PersistenceException(path + " is not single-valued");
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
                ManagedType<?> managedType = forModel(metamodel).filter(rootObject.getClass());
                if (containsAttribute(managedType, property)) {
                    Attribute<?, ?> propertyMapping = managedType.getAttribute(property);
                    Object result = getValue(rootObject, propertyMapping);
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

    private boolean containsAttribute(ManagedType<?> managedType, String name) {
        for (Attribute<?, ?> attributes : managedType.getAttributes()) {
            if (attributes.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private Object getValue(Object target, Attribute<?, ?> attribute) {
        try {
            Member member = attribute.getJavaMember();
            if (member instanceof Field) {
                Field field = (Field)member;
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                target = persistenceUnitUtil.initialize(target);
                return field.get(target);
            } else if (member instanceof Method) {
                Method method = (Method)member;
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method.invoke(target);
            } else {
                throw new UnsupportedOperationException("Unsupported member type " + member.getClass().getName());
            }
        } catch (InvocationTargetException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        }
    }
}
