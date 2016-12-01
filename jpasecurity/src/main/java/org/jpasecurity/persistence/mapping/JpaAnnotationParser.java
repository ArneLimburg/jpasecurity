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
package org.jpasecurity.persistence.mapping;

import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Enumerated;
import javax.persistence.ExcludeDefaultListeners;
import javax.persistence.ExcludeSuperclassListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceException;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpasecurity.CascadeType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.mapping.AbstractSecurityUnitParser;
import org.jpasecurity.mapping.AccessState;
import org.jpasecurity.mapping.DefaultClassMappingInformation;
import org.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory;
import org.jpasecurity.mapping.EntityLifecycleMethods;
import org.jpasecurity.mapping.EntityListener;
import org.jpasecurity.mapping.EntityListenerWrapper;
import org.jpasecurity.mapping.PropertyAccessStrategyFactory;

/**
 * Parses a persistence unit for persistence annotations.
 * <strong>This class is not thread-safe</strong>
 * @author Arne Limburg
 * @author Stefan Hildebrandt
 */
public abstract class JpaAnnotationParser extends AbstractSecurityUnitParser {

    private static final Log LOG = LogFactory.getLog(JpaAnnotationParser.class);

    public JpaAnnotationParser(SecurityUnit securityUnit, ExceptionFactory exceptionFactory) {
        this(securityUnit, new DefaultPropertyAccessStrategyFactory(), exceptionFactory);
    }

    public JpaAnnotationParser(SecurityUnit securityUnit,
                               PropertyAccessStrategyFactory propertyAccessStrategyFactory,
                               ExceptionFactory exceptionFactory) {
        super(securityUnit, propertyAccessStrategyFactory, exceptionFactory);
    }

    protected void parseNamedQueries(Class<?> entityClass) {
        NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
        if (namedQuery != null) {
            addNamedQuery(namedQuery.name(), namedQuery.query());
        }
        NamedQueries queries = entityClass.getAnnotation(NamedQueries.class);
        if (queries != null) {
            for (NamedQuery query: queries.value()) {
                addNamedQuery(query.name(), query.query());
            }
        }
        NamedNativeQuery namedNativeQuery = entityClass.getAnnotation(NamedNativeQuery.class);
        if (namedNativeQuery != null) {
            addNamedNativeQuery(namedNativeQuery.name(), namedNativeQuery.query());
        }
        NamedNativeQueries nativeQueries = entityClass.getAnnotation(NamedNativeQueries.class);
        if (nativeQueries != null) {
            for (NamedNativeQuery query: nativeQueries.value()) {
                addNamedNativeQuery(query.name(), query.query());
            }
        }
    }

    protected boolean excludeDefaultEntityListeners(Class<?> entityClass) {
        return entityClass.getAnnotation(ExcludeDefaultListeners.class) != null;
    }

    protected boolean excludeSuperclassEntityListeners(Class<?> entityClass) {
        return entityClass.getAnnotation(ExcludeSuperclassListeners.class) != null;
    }

    protected void parseEntityLifecycleMethods(DefaultClassMappingInformation classMapping) {
        setEntityLifecycleMethods(classMapping, parseEntityLifecycleMethods(classMapping.getEntityType()));
    }

    protected void parseEntityListeners(DefaultClassMappingInformation classMapping) {
        EntityListeners entityListeners = classMapping.getEntityType().getAnnotation(EntityListeners.class);
        if (entityListeners == null) {
            return;
        }
        for (Class<?> entityListenerClass: entityListeners.value()) {
            try {
                Object entityListener = entityListenerClass.newInstance();
                EntityLifecycleMethods entityLifecycleMethods = parseEntityLifecycleMethods(entityListenerClass);
                EntityListener wrapper
                    = new EntityListenerWrapper(entityListener, entityLifecycleMethods, exceptionFactory);
                addEntityListener(classMapping, entityListenerClass, wrapper);
            } catch (InstantiationException e) {
                throw new PersistenceException("could not instantiate default entity-listener of type " + entityListenerClass.getName(), e);
            } catch (IllegalAccessException e) {
                throw new PersistenceException("could not instantiate default entity-listener of type " + entityListenerClass.getName(), e);
            }
        }
    }

    protected String getEntityName(Class<?> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity == null || entity.name().length() == 0) {
            return super.getEntityName(entityClass);
        } else {
            return entity.name();
        }
    }

    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Access access = mappedClass.getAnnotation(Access.class);
        if (access != null) {
            return access.value() == AccessType.FIELD;
        }
        return super.usesFieldAccess(mappedClass);
    }

    protected AccessState getAccessState(Class<?> mappedClass) {
        AccessState accessState = null;
        Access access = mappedClass.getAnnotation(Access.class);
        if (access != null) {
            if (access.value() == AccessType.FIELD) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(mappedClass.getSimpleName() + " uses field access");
                }
                accessState = AccessState.FIELD_ACCESS;
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(mappedClass.getSimpleName() + " uses property access");
                }
                accessState = AccessState.PROPERTY_ACCESS;
            }
        }
        return getAccessState(mappedClass, accessState);
    }

    protected boolean isMetadataComplete(Class<?> entityClass) {
        return false;
    }

    protected Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess) {
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass == null) {
            return null;
        } else {
            return idClass.value();
        }
    }

    @Override
    protected boolean isTargetTypeOverridden(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        OneToOne oneToOne = annotatedProperty.getAnnotation(OneToOne.class);
        if (oneToOne != null && oneToOne.targetEntity() != null && oneToOne.targetEntity() != void.class) {
            return true;
        }
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.targetEntity() != null && oneToMany.targetEntity() != void.class) {
            return true;
        }
        ManyToOne manyToOne = annotatedProperty.getAnnotation(ManyToOne.class);
        if (manyToOne != null && manyToOne.targetEntity() != null && manyToOne.targetEntity() != void.class) {
            return true;
        }
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null && manyToMany.targetEntity() != null && manyToMany.targetEntity() != void.class) {
            return true;
        }
        return false;
    }

    @Override
    protected Class<?> getTargetType(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        OneToOne oneToOne = annotatedProperty.getAnnotation(OneToOne.class);
        if (oneToOne != null && oneToOne.targetEntity() != null && oneToOne.targetEntity() != void.class) {
            return oneToOne.targetEntity();
        }
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.targetEntity() != null && oneToMany.targetEntity() != void.class) {
            return oneToMany.targetEntity();
        }
        ManyToOne manyToOne = annotatedProperty.getAnnotation(ManyToOne.class);
        if (manyToOne != null && manyToOne.targetEntity() != null && manyToOne.targetEntity() != void.class) {
            return manyToOne.targetEntity();
        }
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null && manyToMany.targetEntity() != null && manyToMany.targetEntity() != void.class) {
            return manyToMany.targetEntity();
        }
        throw exceptionFactory.createMappingException("could not determine target class for property \""
                        + getName(property) + "\" of class "
                        + property.getDeclaringClass().getName());
    }

    protected boolean isMapped(Class<?> mappedClass) {
        return mappedClass.getAnnotation(Entity.class) != null
            || mappedClass.getAnnotation(Embeddable.class) != null
            || mappedClass.getAnnotation(MappedSuperclass.class) != null;
    }

    protected boolean isMappable(Member member) {
        AnnotatedElement annotatedMember = (AnnotatedElement)member;
        if (annotatedMember.isAnnotationPresent(Transient.class)) {
            return false;
        } else {
            return super.isMappable(member);
        }
    }

    protected boolean isMapped(Member member) {
        AnnotatedElement annotatedMember = (AnnotatedElement)member;
        if (!isMappable(member)) {
            return false;
        }
        return isSimplePropertyType(getType(member))
            || isRelationshipProperty(member)
            || annotatedMember.isAnnotationPresent(Id.class)
            || annotatedMember.isAnnotationPresent(Version.class)
            || annotatedMember.isAnnotationPresent(Basic.class)
            || annotatedMember.isAnnotationPresent(Column.class)
            || annotatedMember.isAnnotationPresent(Lob.class)
            || annotatedMember.isAnnotationPresent(Temporal.class)
            || annotatedMember.isAnnotationPresent(Enumerated.class);
    }

    protected boolean isAbstractType(Class<?> type) {
        return type.isAnnotationPresent(MappedSuperclass.class);
    }

    protected boolean isEmbeddable(Class<?> type) {
        return type.isAnnotationPresent(Embeddable.class);
    }

    protected boolean isIdProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        if (annotatedProperty.isAnnotationPresent(Id.class)) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("@Id is present at "
                          + property.getDeclaringClass().getSimpleName() + "." + getName(property));
            }
            return true;
        } else {
            boolean isIdProperty = annotatedProperty.isAnnotationPresent(EmbeddedId.class);
            if (LOG.isTraceEnabled()) {
                if (isIdProperty) {
                    LOG.trace("@EmbeddedId is present at "
                              + property.getDeclaringClass().getSimpleName() + "." + getName(property));
                } else {
                    LOG.trace("No id annotation present at "
                              + property.getDeclaringClass().getSimpleName() + "." + getName(property));
                    Annotation[] annotations = annotatedProperty.getAnnotations();
                    LOG.trace(getName(property) + " has " + annotations.length + " annotations.");
                    for (Annotation annotation: annotations) {
                        Class<?> annotationType = annotation.annotationType();
                        LOG.trace("Found annotation " + annotationType.getName());
                        if ("javax.persistence.Id".equals(annotationType.getName())
                            || "javax.persistence.EmbeddedId".equals(annotation.annotationType().getName())) {
                            LOG.trace("@Id annotation found from another classloader.");
                            LOG.trace("JPA Security is using annotations from location "
                                      + Id.class.getProtectionDomain().getCodeSource().getLocation());
                            LOG.trace(property.getDeclaringClass().getName()
                                      + " is using annotations from location "
                                      + annotationType.getProtectionDomain().getCodeSource().getLocation());
                        }
                    }
                }
            }
            return isIdProperty;
        }
    }

    protected boolean isAccessProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        return annotatedProperty.isAnnotationPresent(Access.class);
    }

    protected boolean isVersionProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        return annotatedProperty.isAnnotationPresent(Version.class);
    }

    protected boolean isGeneratedValue(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        return annotatedProperty.isAnnotationPresent(GeneratedValue.class);
    }

    protected boolean isFetchTypePresent(Member property) {
        return getAnnotationFetchType(property) != null;
    }

    protected org.jpasecurity.FetchType getFetchType(Member property) {
        FetchType fetchType = getAnnotationFetchType(property);
        if (fetchType != null) {
            return org.jpasecurity.FetchType.valueOf(fetchType.name());
        }
        return super.getFetchType(property);
    }

    protected FetchType getAnnotationFetchType(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            return manyToMany.fetch();
        }
        ManyToOne manyToOne = annotatedProperty.getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            return manyToOne.fetch();
        }
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            return oneToMany.fetch();
        }
        OneToOne oneToOne = annotatedProperty.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.fetch();
        }
        return null;
    }

    protected CascadeType[] getCascadeTypes(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            return convert(manyToMany.cascade());
        }
        ManyToOne manyToOne = annotatedProperty.getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            return convert(manyToOne.cascade());
        }
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            return convert(oneToMany.cascade());
        }
        OneToOne oneToOne = annotatedProperty.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return convert(oneToOne.cascade());
        }
        return new CascadeType[0];
    }

    /**
     * The implementation of this method does not really conform with the jpa-spec
     * as it treats embedded objects as relationships.
     * @param property the property to test
     * @return <tt>true</tt>, if the specified property denotes a single-valued relationship property.
     */
    protected boolean isSingleValuedRelationshipProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        return annotatedProperty.isAnnotationPresent(EmbeddedId.class)
            || annotatedProperty.isAnnotationPresent(Embedded.class)
            || annotatedProperty.isAnnotationPresent(ManyToOne.class)
            || annotatedProperty.isAnnotationPresent(OneToOne.class);
    }

    protected boolean isCollectionValuedRelationshipProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        return annotatedProperty.isAnnotationPresent(OneToMany.class)
            || annotatedProperty.isAnnotationPresent(ManyToMany.class);
    }

    protected EntityLifecycleMethods parseEntityLifecycleMethods(Class<?> entityType) {
        EntityLifecycleMethods entityLifecycleMethods = new EntityLifecycleMethods();
        for (Method method: entityType.getDeclaredMethods()) {
            if (method.getAnnotation(PrePersist.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPrePersistMethod(method);
            }
            if (method.getAnnotation(PostPersist.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPostPersistMethod(method);
            }
            if (method.getAnnotation(PreRemove.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPreRemoveMethod(method);
            }
            if (method.getAnnotation(PostRemove.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPostRemoveMethod(method);
            }
            if (method.getAnnotation(PreUpdate.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPreUpdateMethod(method);
            }
            if (method.getAnnotation(PostUpdate.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPostUpdateMethod(method);
            }
            if (method.getAnnotation(PostLoad.class) != null) {
                method.setAccessible(true);
                entityLifecycleMethods.setPostLoadMethod(method);
            }
        }
        return entityLifecycleMethods;
    }

    private CascadeType[] convert(javax.persistence.CascadeType... cascadeTypes) {
        CascadeType[] convertedCascadeTypes = new CascadeType[cascadeTypes.length];
        for (int i = 0; i < cascadeTypes.length; i++) {
            convertedCascadeTypes[i] = CascadeType.valueOf(cascadeTypes[i].name());
        }
        return convertedCascadeTypes;
    }
}
