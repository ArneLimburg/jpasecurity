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
package net.sf.jpasecurity.mapping;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds mapping information for a specific persistence unit.
 * @author Arne Limburg
 */
public class DefaultMappingInformation implements MappingInformation {

    private static final Log LOG = LogFactory.getLog(MappingInformation.class);

    private String persistenceUnitName;
    private Map<String, String> namedQueries = new HashMap<String, String>();
    private Map<Class<?>, ClassMappingInformation> entityTypeMappings
        = new HashMap<Class<?>, ClassMappingInformation>();
    private Map<String, ClassMappingInformation> entityNameMappings;

    /**
     * @param persistenceUnitName the name of the persistence unit
     * @param entityTypeMappings the mapping information of the entities contained in the persistence unit
     * @param namedQueries the named queries contained in the persistence unit
     */
    public DefaultMappingInformation(String persistenceUnitName,
                                     Map<Class<?>, ? extends ClassMappingInformation> entityTypeMappings,
                                     Map<String, String> namedQueries) {
        this.persistenceUnitName = persistenceUnitName;
        this.entityTypeMappings = (Map<Class<?>, ClassMappingInformation>)entityTypeMappings;
        initializeEntityNameMappings();
        this.namedQueries = namedQueries;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public Set<String> getNamedQueryNames() {
        return Collections.unmodifiableSet(namedQueries.keySet());
    }

    public String getNamedQuery(String name) {
        return namedQueries.get(name);
    }

    public Collection<Class<?>> getPersistentClasses() {
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

    public ClassMappingInformation getClassMapping(Class<?> entityType) {
        ClassMappingInformation classMapping = entityTypeMappings.get(entityType);
        while (classMapping == null && entityType != null) {
            entityType = entityType.getSuperclass();
            classMapping = entityTypeMappings.get(entityType);
        }
        if (classMapping == null) {
            throw new PersistenceException("Could not find mapping for entity with type \"" + (entityType == null? null: entityType.getName()) + '"');
        }
        return classMapping;
    }

    public boolean containsClassMapping(String entityName) {
        return entityNameMappings.containsKey(entityName);
    }

    public ClassMappingInformation getClassMapping(String entityName) {
        ClassMappingInformation classMapping = entityNameMappings.get(entityName);
        if (classMapping == null) {
            LOG.info("Content of entityNameMappings:");
            logEntityNameMappings();
            throw new PersistenceException("Could not find mapping for entity with name \"" + entityName + '"');
        }
        return classMapping;
    }

    public Class<?> getType(String path, Set<TypeDefinition> typeDefinitions) {
        String[] entries = path.split("\\.");
        Class<?> type = getAliasType(new Alias(entries[0]), typeDefinitions);
        for (int i = 1; i < entries.length; i++) {
            type = getClassMapping(type).getPropertyMapping(entries[i]).getProperyType();
        }
        return type;
    }

    private void initializeEntityNameMappings() {
        entityNameMappings = new HashMap<String, ClassMappingInformation>();
        for (ClassMappingInformation classMapping: entityTypeMappings.values()) {
            entityNameMappings.put(classMapping.getEntityName(), classMapping);
            entityNameMappings.put(classMapping.getEntityType().getName(), classMapping);
        }
        LOG.info("Content of entityNameMappings after initialization:");
        logEntityNameMappings();
    }

    private Class<?> getAliasType(Alias alias, Set<TypeDefinition> typeDefinitions) {
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (alias.equals(typeDefinition.getAlias())) {
                return typeDefinition.getType();
            }
        }
        throw new TypeNotPresentException(alias.getName(), null);
    }

   private void logEntityNameMappings() {
      for (Map.Entry<String, ClassMappingInformation> stringClassMappingInformationEntry : entityNameMappings
         .entrySet()) {
         LOG.info(stringClassMappingInformationEntry.getKey()
            + ": " + stringClassMappingInformationEntry.getValue().getEntityType());
      }
   }
}
