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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * This class contains mapping information of a specific class.
 * @author Arne Limburg
 */
public final class DefaultClassMappingInformation implements ClassMappingInformation {

    private String entityName;
    private Class<?> entityType;
    private DefaultClassMappingInformation superclassMapping;
    private Set<ClassMappingInformation> subclassMappings = new HashSet<ClassMappingInformation>();
    private ClassMappingInformation idClassMapping;
    private boolean embeddable;
    private boolean fieldAccess;
    private boolean metadataComplete;
    private boolean excludeSuperclassEntityListeners;
    private Map<String, AbstractPropertyMappingInformation> propertyMappings
        = new HashMap<String, AbstractPropertyMappingInformation>();
    private List<EntityListener> defaultEntityListeners = Collections.EMPTY_LIST;
    private Map<Class<?>, EntityListener> entityListeners = new LinkedHashMap<Class<?>, EntityListener>();
    private EntityListener entityLifecyleAdapter;
    private ExceptionFactory exceptionFactory;

    public DefaultClassMappingInformation(String entityName,
                                          Class<?> entityType,
                                          DefaultClassMappingInformation superclassMapping,
                                          ClassMappingInformation idClass,
                                          boolean embeddable,
                                          boolean usesFieldAccess,
                                          boolean metadataComplete,
                                          ExceptionFactory exceptionFactory) {
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
        this.idClassMapping = idClass;
        this.embeddable = embeddable;
        this.fieldAccess = usesFieldAccess;
        this.metadataComplete = metadataComplete;
        this.exceptionFactory = exceptionFactory;
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

    public ClassMappingInformation getIdClassMapping() {
        return idClassMapping;
    }

    void setIdClassMapping(ClassMappingInformation idClassMapping) {
        this.idClassMapping = idClassMapping;
    }

    public boolean isEmbeddable() {
        return embeddable;
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

    public boolean containsPropertyMapping(String propertyName) {
        return propertyMappings.containsKey(propertyName)
               || (superclassMapping != null && superclassMapping.containsPropertyMapping(propertyName));
    }

    public AbstractPropertyMappingInformation getPropertyMapping(String propertyName) {
        AbstractPropertyMappingInformation propertyMapping = propertyMappings.get(propertyName);
        if (propertyMapping == null && superclassMapping != null) {
            return superclassMapping.getPropertyMapping(propertyName);
        }
        if (propertyMapping == null) {
            throw exceptionFactory.createPropertyNotFoundException(getEntityType(), propertyName);
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

    void addPropertyMapping(AbstractPropertyMappingInformation propertyMappingInformation) {
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

    public List<PropertyMappingInformation> getVersionPropertyMappings() {
        List<PropertyMappingInformation> versionPropertyMappings = new ArrayList<PropertyMappingInformation>();
        for (PropertyMappingInformation propertyMapping: propertyMappings.values()) {
            if (propertyMapping.isVersionProperty()) {
                versionPropertyMappings.add(propertyMapping);
            }
        }
        if (versionPropertyMappings.size() > 0) {
            return Collections.unmodifiableList(versionPropertyMappings);
        } else if (superclassMapping != null) {
            return superclassMapping.getVersionPropertyMappings();
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
        entityLifecyleAdapter = new EntityLifecycleAdapter(entityLifecycleMethods, exceptionFactory);
    }

    public Object newInstance() {
        return ReflectionUtils.newInstance(getEntityType());
    }

    public Object getId(Object entity) {
        if (embeddable) {
            return null;
        }
        List<PropertyMappingInformation> idProperties = getIdPropertyMappings();
        if (idProperties.size() == 0) {
            String error = "Id property required for class " + getEntityType().getName();
            throw exceptionFactory.createMappingException(error);
        } else if (idProperties.size() == 1) {
            return idProperties.get(0).getPropertyValue(entity);
        } else {
            try {
                ClassMappingInformation idClassMapping = getIdClassMapping();
                Object id = ReflectionUtils.newInstance(idClassMapping.getEntityType());
                for (PropertyMappingInformation idProperty: idProperties) {
                    Object value = idProperty.getPropertyValue(entity);
                    if (idProperty.isRelationshipMapping()) {
                        value = ((RelationshipMappingInformation)idProperty).getRelatedClassMapping().getId(value);
                    }
                    idClassMapping.getPropertyMapping(idProperty.getPropertyName()).setPropertyValue(id, value);
                }
                return id;
            } catch (RuntimeException e) {
                throw exceptionFactory.createRuntimeException(e);
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

    public String toString() {
        return getClass().getSimpleName() + "[entityType=" + entityType.getSimpleName() + "]";
    }

    private void fireLifecycleEvent(Object entity, EntityListenerClosure closure) {
        for (EntityListener entityListener: defaultEntityListeners) {
            closure.call(entityListener);
        }
        if (!excludeSuperclassEntityListeners) {
            DefaultClassMappingInformation superclassMapping = this.superclassMapping;
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
        DefaultClassMappingInformation superclassMapping = this.superclassMapping;
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
