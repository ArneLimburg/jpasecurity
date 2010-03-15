/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.util.SystemMapKey;

/**
 * @author Arne Limburg
 */
public class SecureObjectCache extends EntityPersister {

    private Map<ClassMappingInformation, Map<Object, SecureEntity>> secureEntities
        = new HashMap<ClassMappingInformation, Map<Object, SecureEntity>>();
    private Map<SystemMapKey, SecureCollection<?>> secureCollections
        = new HashMap<SystemMapKey, SecureCollection<?>>();

    public SecureObjectCache(MappingInformation mappingInformation,
                             EntityManager entityManager,
                             AccessManager accessManager) {
        super(mappingInformation, entityManager, accessManager);
    }

    public SecureCollection<?> getSecureCollection(Collection<?> unsecureCollection) {
        SecureCollection<?> secureCollection = secureCollections.get(new SystemMapKey(unsecureCollection));
        if (secureCollection != null) {
            return secureCollection;
        }
        secureCollection = (SecureCollection<?>)super.getSecureObject(unsecureCollection);
        secureCollections.put(new SystemMapKey(unsecureCollection), secureCollection);
        return secureCollection;
    }

    public <E> E getSecureObject(E unsecureObject) {
        if (unsecureObject == null) {
            return null;
        }
        if (unsecureObject instanceof Collection) {
            return (E)getSecureCollection((Collection<?>)unsecureObject);
        }
        ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        if (classMapping == null) {
            throw new PersistenceException("Unknown entity type " + unsecureObject.getClass());
        }
        Map<Object, SecureEntity> entities = secureEntities.get(classMapping);
        if (entities == null) {
            entities = new HashMap<Object, SecureEntity>();
            secureEntities.put(classMapping, entities);
        }
        Object id = classMapping.getId(unsecureObject);
        SecureEntity entity = entities.get(id);
        if (entity != null) {
            return (E)entity;
        }
        Object secureObject = super.getSecureObject(unsecureObject);
        if (secureObject instanceof SecureEntity) {
            entities.put(id, (SecureEntity)secureObject);
        }
        return (E)secureObject;
    }

    public <E> Collection<E> getSecureObjects(Class<E> type) {
        List<E> secureObjects = new ArrayList<E>();
        for (Map.Entry<ClassMappingInformation, Map<Object, SecureEntity>> entities: secureEntities.entrySet()) {
            if (entities.getKey().getEntityType().isAssignableFrom(type)) {
                secureObjects.addAll(((Collection<E>)entities.getValue().values()));
            }
        }
        secureObjects.addAll(super.getSecureObjects(type));
        return secureObjects;
    }

    public void preFlush() {
        super.preFlush();
        for (Map<Object, SecureEntity> entities: secureEntities.values()) {
            for (SecureEntity entity: entities.values()) {
                entity.flush();
            }
        }
        //we must not flush collections here, since they are flushed by their owning entities
    }

    public void clear() {
        super.clear();
        secureEntities.clear();
        //collections are flushed by their corresponding entities
    }
}
