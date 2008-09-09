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
package net.sf.jpasecurity.persistence.mapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.util.SecureCollection;
import net.sf.jpasecurity.util.SecureEntityHandler;
import net.sf.jpasecurity.util.SecureList;
import net.sf.jpasecurity.util.SecureSet;
import net.sf.jpasecurity.util.SecureSortedSet;

/**
 * @author Arne Limburg
 */
public class EntityInvocationHandler implements MethodInterceptor {

    private ClassMappingInformation mapping;
    private SecureEntityHandler entityHandler;
    private boolean initialized;
    private Object entity;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();
    
    public EntityInvocationHandler(ClassMappingInformation mapping,
                                   SecureEntityHandler entityHandler,
                                   Object entity) {
        this.mapping = mapping;
        this.entityHandler = entityHandler;
        this.entity = entity;
    }
    
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (!initialized) {
            initialize(object);
        }
        Object result = methodProxy.invokeSuper(object, args);
        updatedChangedProperties(object);
        return result;
    }

    private void initialize(Object object) {
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            setPropertyValue(propertyMapping, entity, object);
        }
        initialized = true;
    }
    
    private void updatedChangedProperties(Object object) {
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            if (propertyMapping.getPropertyValue(object) != propertyValues.get(propertyMapping.getPropertyName())) {
                setPropertyValue(propertyMapping, object, entity);
            }
        }
    }
    
    private void setPropertyValue(PropertyMappingInformation propertyMapping, Object source, Object target) {
        Object value;
        if (propertyMapping instanceof RelationshipMappingInformation) {
            RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
            if (relationshipMapping instanceof SingleValuedRelationshipMappingInformation) {
                Object relatedEntity = propertyMapping.getPropertyValue(source);
                value = entityHandler.getSecureEntity(relatedEntity);
            } else {
                Collection<?> relatedEntities = (Collection<?>)propertyMapping.getPropertyValue(source);
                if (relatedEntities instanceof List) {
                    value = new SecureList((List)relatedEntities, entityHandler);
                } else if (relatedEntities instanceof SortedSet) {
                    value = new SecureSortedSet((SortedSet)relatedEntities, entityHandler);
                } else if (relatedEntities instanceof Set) {
                    value = new SecureSet((Set)relatedEntities, entityHandler);
                } else {
                    value = new SecureCollection(relatedEntities, entityHandler);
                }
            }
        } else {
            value = propertyMapping.getPropertyValue(source);
        }
        propertyMapping.setPropertyValue(target, value);
        propertyValues.put(propertyMapping.getPropertyName(), value);        
    }
}
