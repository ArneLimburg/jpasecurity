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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

/**
 * This class contains mapping information of a specific class.
 * @author Arne Limburg
 */
public final class ClassMappingInformation {

    private String entityName;
    private Class<?> entityType;
    private ClassMappingInformation superclassMapping;
    private Set<ClassMappingInformation> subclassMappings = new HashSet<ClassMappingInformation>();
    private Class<?> idClass;
    private boolean fieldAccess;
    private boolean metadataComplete;
    private boolean excludeSuperclassEntityListeners;
    private Map<String, PropertyMappingInformation> propertyMappings
        = new HashMap<String, PropertyMappingInformation>();
    private List<EntityListener> defaultEntityListeners = Collections.EMPTY_LIST;
    private Map<Class<?>, EntityListener> entityListeners = new LinkedHashMap<Class<?>, EntityListener>();
    private EntityListener entityLifecyleAdapter;

    public ClassMappingInformation(String entityName,
                                   Class<?> entityType,
                                   ClassMappingInformation superclassMapping,
                                   Class<?> idClass,
                                   boolean usesFieldAccess,
                                   boolean metadataComplete) {
        if (entityName == null) {
            throw new IllegalArgumentException("entityName may not be null");
        }
        if (entityType == null) {
            throw new IllegalArgumentException("entityType may not be null");
        }
        this.entityName = entityName;
        this.entityType = entityType;
        this.superclassMapping = superclassMapping;
        if (superclassMapping != null) {
            superclassMapping.subclassMappings.add(this);
        }
        this.idClass = idClass;
        this.fieldAccess = usesFieldAccess;
        this.metadataComplete = metadataComplete;
    }

    public String getEntityName() {
        return entityName;
    }

    void setEntityName(String entityName) {
        if (entityName == null) {
            throw new IllegalArgumentException("entityName may not be null");
        }
        this.entityName = entityName;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public Set<ClassMappingInformation> getSubclassMappings() {
        return Collections.unmodifiableSet(subclassMappings);
    }

    public Class<?> getIdClass() {
        return idClass;
    }

    void setIdClass(Class<?> idClass) {
        this.idClass = idClass;
    }

    public boolean usesFieldAccess() {
        return fieldAccess;
    }

    public boolean usesPropertyAccess() {
        return !fieldAccess;
    }

    void setFieldAccess(boolean fieldAccess) {
        this.fieldAccess = fieldAccess;
    }

    public boolean isMetadataComplete() {
        return metadataComplete;
    }

    void setMetadataComplete(boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }

    public boolean areSuperclassEntityListenersExcluded() {
        return excludeSuperclassEntityListeners;
    }

    void setSuperclassEntityListenersExcluded(boolean excludeSuperclassEntityListeners) {
        this.excludeSuperclassEntityListeners = excludeSuperclassEntityListeners;
    }

    void clearPropertyMappings() {
        propertyMappings.clear();
    }

    public PropertyMappingInformation getPropertyMapping(String propertyName) {
        PropertyMappingInformation propertyMapping = propertyMappings.get(propertyName);
        if (propertyMapping == null && superclassMapping != null) {
            return superclassMapping.getPropertyMapping(propertyName);
        }
        return propertyMapping;
    }

    public List<PropertyMappingInformation> getPropertyMappings() {
        List<PropertyMappingInformation> propertyMappings = new ArrayList<PropertyMappingInformation>();
        propertyMappings.addAll(this.propertyMappings.values());
        if (superclassMapping != null) {
            propertyMappings.addAll(superclassMapping.getPropertyMappings());
        }
        return Collections.unmodifiableList(propertyMappings);
    }

    void addPropertyMapping(PropertyMappingInformation propertyMappingInformation) {
        propertyMappings.put(propertyMappingInformation.getPropertyName(), propertyMappingInformation);
    }

    public List<PropertyMappingInformation> getIdPropertyMappings() {
        List<PropertyMappingInformation> idPropertyMappings = new ArrayList<PropertyMappingInformation>();
        for (PropertyMappingInformation propertyMapping: propertyMappings.values()) {
            if (propertyMapping.isIdProperty()) {
                idPropertyMappings.add(propertyMapping);
            }
        }
        if (idPropertyMappings.size() > 0) {
            return Collections.unmodifiableList(idPropertyMappings);
        } else if (superclassMapping != null) {
            return superclassMapping.getIdPropertyMappings();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    void setDefaultEntityListeners(List<EntityListener> defaultEntityListeners) {
        this.defaultEntityListeners = defaultEntityListeners;
    }

    void addEntityListener(Class<?> type, EntityListener entityListener) {
        entityListeners.put(type, entityListener);
    }

    void setEntityLifecycleMethods(EntityLifecycleMethods entityLifecycleMethods) {
        entityLifecyleAdapter = new EntityLifecycleAdapter(entityLifecycleMethods);
    }

    public Object newInstance() {
        Constructor<?> constructor;
        try {
            constructor = getEntityType().getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new PersistenceException("No default constructor found for entity-type " + getEntityType().getName());
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (InvocationTargetException e) {
            throw new PersistenceException(e.getTargetException());
        }
    }

    public Object getId(Object entity) {
        List<PropertyMappingInformation> idProperties = getIdPropertyMappings();
        if (idProperties.size() == 0) {
            throw new PersistenceException("Id property required for class " + getEntityType().getName());
        } else if (idProperties.size() == 1) {
            return idProperties.get(0).getPropertyValue(entity);
        } else {
            try {
                Object id = getIdClass().newInstance();
                for (PropertyMappingInformation idProperty: idProperties) {
                    idProperty.setPropertyValue(id, idProperty.getPropertyValue(entity));
                }
                return id;
            } catch (InstantiationException e) {
                throw new PersistenceException(e);
            } catch (IllegalAccessException e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void prePersist(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.prePersist(entity);
            }
        });
    }

    public void postPersist(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.postPersist(entity);
            }
        });
    }

    public void preRemove(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.preRemove(entity);
            }
        });
    }

    public void postRemove(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.postRemove(entity);
            }
        });
    }

    public void preUpdate(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.preUpdate(entity);
            }
        });
    }

    public void postUpdate(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.postUpdate(entity);
            }
        });
    }

    public void postLoad(final Object entity) {
        fireLifecycleEvent(entity, new EntityListenerClosure() {
            public void call(EntityListener entityListener) {
                entityListener.postLoad(entity);
            }
        });
    }

    private void fireLifecycleEvent(Object entity, EntityListenerClosure closure) {
        for (EntityListener entityListener: defaultEntityListeners) {
            closure.call(entityListener);
        }
        if (!excludeSuperclassEntityListeners) {
            ClassMappingInformation superclassMapping = this.superclassMapping;
            while (superclassMapping != null && !superclassMapping.excludeSuperclassEntityListeners) {
                for (EntityListener entityListener: superclassMapping.entityListeners.values()) {
                    closure.call(entityListener);
                }
                superclassMapping = superclassMapping.superclassMapping;
            }
        }
        for (EntityListener entityListener: entityListeners.values()) {
            closure.call(entityListener);
        }
        closure.call(entityLifecyleAdapter);
        ClassMappingInformation superclassMapping = this.superclassMapping;
        while (superclassMapping != null && !superclassMapping.excludeSuperclassEntityListeners) {
            //TODO handle overridden lifecycle methods
            closure.call(superclassMapping.entityLifecyleAdapter);
            superclassMapping = superclassMapping.superclassMapping;
        }
    }

    private interface EntityListenerClosure {
        void call(EntityListener entityListener);
    }
}
