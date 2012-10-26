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
package net.sf.jpasecurity.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.ExceptionFactory;

/**
 * This class holds mapping information for a specific persistence unit.
 * @author Arne Limburg
 */
public class DefaultMappingInformation implements MappingInformation {

    private String persistenceUnitName;
    private final Map<String, String> namedNativeQueries;
    private Map<String, String> namedQueries = new HashMap<String, String>();
    private Map<Class<?>, ClassMappingInformation> entityTypeMappings
        = new HashMap<Class<?>, ClassMappingInformation>();
    private Map<String, ClassMappingInformation> entityNameMappings;
    private ExceptionFactory exceptionFactory;

    /**
     * @param persistenceUnitName the name of the persistence unit
     * @param entityTypeMappings the mapping information of the entities contained in the persistence unit
     * @param namedQueries the named queries contained in the persistence unit
     * @param nativeNamedQueries the named native queries contained in the persistence unit
     */
    public DefaultMappingInformation(String persistenceUnitName,
                                     Map<Class<?>, ? extends ClassMappingInformation> entityTypeMappings,
                                     Map<String, String> namedQueries,
                                     Map<String, String> namedNativeQueries, ExceptionFactory exceptionFactory) {
        this.persistenceUnitName = persistenceUnitName;
        this.namedNativeQueries = namedNativeQueries;
        this.entityTypeMappings = (Map<Class<?>, ClassMappingInformation>)entityTypeMappings;
        this.namedQueries = namedQueries;
        this.exceptionFactory = exceptionFactory;
    }

    public String getSecurityUnitName() {
        return persistenceUnitName;
    }

    public Set<String> getNamedQueryNames() {
        return Collections.unmodifiableSet(namedQueries.keySet());
    }

    public String getNamedQuery(String name) {
        return namedQueries.get(name);
    }

    public Set<String> getNamedNativeQueryNames() {
        return Collections.unmodifiableSet(namedNativeQueries.keySet());
    }

    public String getNamedNativeQuery(String name) {
        return namedNativeQueries.get(name);
    }

    public Collection<Class<?>> getSecureClasses() {
        return Collections.unmodifiableSet(entityTypeMappings.keySet());
    }

    public boolean containsClassMapping(Class<?> entityType) {
        while (entityType != null) {
            if (entityTypeMappings.containsKey(entityType)) {
                return true;
            }
            entityType = entityType.getSuperclass();
        }
        return false;
    }

    public ClassMappingInformation getClassMapping(Class<?> type) {
        Class<?> entityType = type;
        ClassMappingInformation classMapping = entityTypeMappings.get(entityType);
        while (classMapping == null && entityType != null) {
            entityType = entityType.getSuperclass();
            classMapping = entityTypeMappings.get(entityType);
        }
        if (classMapping == null) {
            throw exceptionFactory.createTypeNotFoundException(type);
        }
        return classMapping;
    }

    public boolean containsClassMapping(String entityName) {
        if (entityNameMappings == null) {
            initializeEntityNameMappings();
        }
        return entityNameMappings.containsKey(entityName);
    }

    public ClassMappingInformation getClassMapping(String entityName) {
        if (entityNameMappings == null) {
            initializeEntityNameMappings();
        }
        ClassMappingInformation classMapping = entityNameMappings.get(entityName);
        if (classMapping == null) {
            throw exceptionFactory.createTypeNotFoundException(entityName);
        }
        return classMapping;
    }

    public Collection<ClassMappingInformation> resolveClassMappings(Class<?> type) {
        Set<ClassMappingInformation> resolvedMappings = new HashSet<ClassMappingInformation>();
        for (Map.Entry<Class<?>, ClassMappingInformation> classMappingEntry: entityTypeMappings.entrySet()) {
            if (type.isAssignableFrom(classMappingEntry.getKey())) {
                resolvedMappings.add(classMappingEntry.getValue());
            }
        }
        return Collections.unmodifiableCollection(resolvedMappings);
    }

    public PropertyMappingInformation getPropertyMapping(Path path, Set<TypeDefinition> typeDefinitions) {
        if (path.isKeyPath()) {
            return getPropertyMapping(getKeyType(path.getRootAlias(), typeDefinitions), path);
        } else {
            return getPropertyMapping(getType(path.getRootAlias(), typeDefinitions), path);
        }
    }

    public PropertyMappingInformation getPropertyMapping(Class<?> rootType, Path path) {
        PropertyMappingInformation propertyMapping = null;
        for (String propertyName: path.getSubpath().split("\\.")) {
            propertyMapping = getClassMapping(rootType).getPropertyMapping(propertyName);
            rootType = propertyMapping.getProperyType();
        }
        return propertyMapping;
    }

    public boolean isMapPath(Path path, Set<TypeDefinition> typeDefinitions) {
        if (!path.hasSubpath()) {
            return false;
        }
        return getPropertyMapping(path, typeDefinitions).isMapMapping();
    }

    public Class<?> getKeyType(Alias alias, Set<TypeDefinition> typeDefinitions) {
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (alias.equals(typeDefinition.getAlias())) {
                return getKeyType(typeDefinition.getJoinPath(), typeDefinitions);
            }
        }
        throw new TypeNotPresentException(alias.getName(), null);
    }

    public Class<?> getKeyType(Path path, Set<TypeDefinition> typeDefinitions) {
        return ((MapValuedRelationshipMappingInformation)getPropertyMapping(path, typeDefinitions)).getKeyClass();
    }

    public <T> Class<T> getType(Path path, Set<TypeDefinition> typeDefinitions) {
        if (path.hasSubpath()) {
            return (Class<T>)getPropertyMapping(path, typeDefinitions).getProperyType();
        } else {
            return getType(path.getRootAlias(), typeDefinitions);
        }
    }

    public <T> Class<T> getType(Alias alias, Set<TypeDefinition> typeDefinitions) {
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (alias.equals(typeDefinition.getAlias())) {
                return typeDefinition.<T>getType();
            }
        }
        throw new TypeNotPresentException(alias.getName(), null);
    }

    private void initializeEntityNameMappings() {
        entityNameMappings = new HashMap<String, ClassMappingInformation>();
        for (ClassMappingInformation classMapping: entityTypeMappings.values()) {
            entityNameMappings.put(classMapping.getEntityName(), classMapping);
            entityNameMappings.put(classMapping.getEntityType().getName(), classMapping);
        }
    }
}
