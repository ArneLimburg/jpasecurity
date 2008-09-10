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
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
        if (method.getName().equals("getUnsecureEntity") && method.getParameterTypes().length == 0) {
            return entity;
        }
        if (!initialized) {
            initialize(object);
        }
        Object result = methodProxy.invokeSuper(object, args);
        updatedChangedProperties(object);
        return result;
    }
    
    private void initialize(Object object) {
        object = unproxy(object);
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(entity);
            if (propertyMapping instanceof RelationshipMappingInformation) {
                value = entityHandler.getSecureObject(value);
//                if (relationshipMapping instanceof SingleValuedRelationshipMappingInformation) {
//                } else {
//                    Collection<?> relatedEntities = (Collection<?>)propertyMapping.getPropertyValue(source);
//                    if (relatedEntities instanceof List) {
//                        value = new SecureList((List)relatedEntities, entityHandler);
//                    } else if (relatedEntities instanceof SortedSet) {
//                        value = new SecureSortedSet((SortedSet)relatedEntities, entityHandler);
//                    } else if (relatedEntities instanceof Set) {
//                        value = new SecureSet((Set)relatedEntities, entityHandler);
//                    } else {
//                        value = new DefaultSecureCollection(relatedEntities, entityHandler);
//                    }
//                }
            }
            propertyMapping.setPropertyValue(object, value);
            propertyValues.put(propertyMapping.getPropertyName(), value);        
        }
        initialized = true;
    }
    
    private void updatedChangedProperties(Object object) {
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(object);
            if (value != propertyValues.get(propertyMapping.getPropertyName())) {
                propertyMapping.setPropertyValue(entity, entityHandler.getUnsecureObject(value));
                if (propertyMapping instanceof RelationshipMappingInformation) {
                    value = entityHandler.getSecureObject(object);
                    propertyMapping.setPropertyValue(object, value);
                }
                propertyValues.put(propertyMapping.getPropertyName(), value);
            }
        }
    }

    private Object unproxy(Object object) {
        try {
            Class<?> hibernateProxy = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            if (!hibernateProxy.isInstance(object)) {
                return object;
            }
            Class<?> lazyInitializer = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer"); 
            Object lazyInitializerInstance = hibernateProxy.getMethod("getHibernateLazyInitializer").invoke(object);
            return lazyInitializer.getMethod("getImplementation").invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }
}
