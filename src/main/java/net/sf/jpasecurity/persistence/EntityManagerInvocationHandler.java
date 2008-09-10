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
package net.sf.jpasecurity.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.cglib.proxy.Enhancer;
import net.sf.jpasecurity.jpql.compiler.EntityFilter;
import net.sf.jpasecurity.jpql.compiler.FilterResult;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.persistence.mapping.ClassMappingInformation;
import net.sf.jpasecurity.persistence.mapping.EntityInvocationHandler;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.mapping.SecureEntityHandler;
import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRule;

/**
 * @author Arne Limburg
 */
public class EntityManagerInvocationHandler implements SecureEntityHandler, InvocationHandler {

    public static final String CREATE_QUERY_METHOD_NAME = "createQuery";

    private EntityManager entityManager;
    private AuthenticationProvider authenticationProvider;
    private MappingInformation mappingInformation;
    private EntityFilter entityFilter;
    private Map<Class, Map<Object, Object>> secureEntities;

    EntityManagerInvocationHandler(EntityManager entityManager,
                                   MappingInformation mappingInformation,
                                   AuthenticationProvider authenticationProvider,
                                   List<AccessRule> accessRules) {
        this.entityManager = entityManager;
        this.authenticationProvider = authenticationProvider;
        this.mappingInformation = mappingInformation;
        this.entityFilter = new EntityFilter(entityManager, mappingInformation, accessRules);
        this.secureEntities = new HashMap<Class, Map<Object,Object>>();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (isCreateQueryMethod(method)) {
                return createQuery((EntityManager)proxy, (String)args[0]);
            } else {
                return method.invoke(entityManager, args);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    private boolean isCreateQueryMethod(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return method.getName().equals(CREATE_QUERY_METHOD_NAME)
            && parameterTypes.length == 1
            && parameterTypes[0] == String.class;
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    private Query createQuery(EntityManager entityManagerProxy, String qlString) {
        Object user = getCurrentUser();
        Set<Object> roles = getCurrentRoles();
        FilterResult filterResult = entityFilter.filterQuery(qlString, user, roles);
        Query query = entityManager.createQuery(filterResult.getQuery());
        if (filterResult.getUserParameterName() != null) {
            query.setParameter(filterResult.getUserParameterName(), user);
        }
        if (filterResult.getRoleParameters() != null) {
            for (Map.Entry<String, Object> roleParameter: filterResult.getRoleParameters().entrySet()) {
                query.setParameter(roleParameter.getKey(), roleParameter.getValue());
            }
        }
        return (Query)Proxy.newProxyInstance(query.getClass().getClassLoader(),
                                             new Class[] {Query.class},
                                             new QueryInvocationHandler(this, query));
    }

    public boolean isAccessible(Object entity) {
        try {
            return entityFilter.isAccessible(entity, getCurrentUser(), getCurrentRoles());
        } catch (NotEvaluatableException e) {
            throw new SecurityException(e);
        }
    }
    
    public Object getSecureObject(Object object) {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(object.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(object.getClass() + " is not mapped");
        }
        Object id = mapping.getId(object);
        Map<Object, Object> entities = secureEntities.get(mapping.getEntityType());
        if (entities != null) {
            Object secureEntity = entities.get(id);
            if (secureEntity != null) {
                return secureEntity;
            }
        } else {
            entities = new HashMap<Object, Object>();
            secureEntities.put(mapping.getEntityType(), entities);
        }
        Object secureEntity = createSecureEntity(mapping, object);
        entities.put(id, secureEntity);
        return secureEntity;
    }
    
    public Object getUnsecureObject(Object object) {
        if (object instanceof SecureEntity) {
            return ((SecureEntity)object).getUnsecureEntity();
        } else {
            return object;
        }
    }
    
    private Object getCurrentUser() {
        Object user = authenticationProvider.getUser();
        if (user != null) {
            ClassMappingInformation userClassMapping = mappingInformation.getClassMapping(user.getClass());
            if (userClassMapping != null) {
                Object id = userClassMapping.getId(user);
                user = entityManager.getReference(userClassMapping.getEntityType(), id);
            }
        }
        return user;
    }
    
    private Set<Object> getCurrentRoles() {
        Collection<Object> authorizedRoles = authenticationProvider.getRoles();
        Set<Object> roles = new HashSet<Object>();
        if (authorizedRoles != null) {
            for (Object role: authorizedRoles) {
                ClassMappingInformation roleClassMapping = mappingInformation.getClassMapping(role.getClass());
                if (roleClassMapping == null) {
                    roles.add(role);
                } else {
                    Object id = roleClassMapping.getId(role);
                    roles.add(entityManager.getReference(roleClassMapping.getEntityType(), id));
                }
            }
        }
        return roles;
    }
    
    private Object createSecureEntity(ClassMappingInformation mapping, Object entity) {
        return Enhancer.create(mapping.getEntityType(),
                               new Class[] {SecureEntity.class},
                               new EntityInvocationHandler(mapping, this, entity));
    }
}
