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
import java.util.List;
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
        return methodProxy.invokeSuper(object, args);
    }

    private void initialize(Object object) {
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            if (propertyMapping instanceof RelationshipMappingInformation) {
                RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
                if (relationshipMapping instanceof SingleValuedRelationshipMappingInformation) {
                    Object relatedEntity = propertyMapping.getPropertyValue(entity);
                    relatedEntity = entityHandler.getSecureEntity(relatedEntity);
                    propertyMapping.setPropertyValue(object, relatedEntity);
                } else {
                    Collection<?> relatedEntities = (Collection<?>)propertyMapping.getPropertyValue(entity);
                    if (relatedEntities instanceof List) {
                        propertyMapping.setPropertyValue(object, new SecureList((List)relatedEntities, entityHandler));
                    } else if (relatedEntities instanceof SortedSet) {
                        propertyMapping.setPropertyValue(object, new SecureSortedSet((SortedSet)relatedEntities, entityHandler));
                    } else if (relatedEntities instanceof Set) {
                        propertyMapping.setPropertyValue(object, new SecureSet((Set)relatedEntities, entityHandler));
                    } else {
                        propertyMapping.setPropertyValue(object, new SecureCollection(relatedEntities, entityHandler));
                    }
                }
            } else {
                propertyMapping.setPropertyValue(object, propertyMapping.getPropertyValue(entity));
            }
        }
        initialized = true;
    }
}
