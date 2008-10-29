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
package net.sf.jpasecurity.mapping.parser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.net.URL;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Parses a persistence unit for persistence annotations.
 * <strong>This class is not thread-safe</strong>
 * @author Arne Limburg
 */
public class JpaAnnotationParser extends AbstractMappingParser {

    protected void parsePersistenceUnit(PersistenceUnitInfo persistenceUnit) {
        if (!persistenceUnit.excludeUnlistedClasses()) {
            if (persistenceUnit.getPersistenceUnitRootUrl() != null) {
                parse(persistenceUnit.getPersistenceUnitRootUrl());
            }
        }
        for (URL url: persistenceUnit.getJarFileUrls()) {
            parse(url);
        }
        for (String className: persistenceUnit.getManagedClassNames()) {
            parse(getClass(className));
        }
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
    }

    protected Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess) {
        IdClass idClass = entityClass.getAnnotation(IdClass.class);
        if (idClass == null) {
            return null;
        } else {
            return idClass.value();
        }
    }

    protected Class<?> getTargetType(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.targetEntity() != null && oneToMany.targetEntity() != void.class) {
            return oneToMany.targetEntity();
        }
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null && manyToMany.targetEntity() != null && manyToMany.targetEntity() != void.class) {
            return manyToMany.targetEntity();
        }
        return super.getTargetType(property);
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
        if (annotatedMember.isAnnotationPresent(Transient.class)) {
            return false;
        }
        return isRelationshipProperty(member)
            || annotatedMember.isAnnotationPresent(Id.class)
            || annotatedMember.isAnnotationPresent(EmbeddedId.class)
            || annotatedMember.isAnnotationPresent(Version.class)
            || annotatedMember.isAnnotationPresent(Basic.class)
            || annotatedMember.isAnnotationPresent(Column.class)
            || annotatedMember.isAnnotationPresent(Lob.class)
            || annotatedMember.isAnnotationPresent(Temporal.class)
            || annotatedMember.isAnnotationPresent(Enumerated.class)
            || annotatedMember.isAnnotationPresent(Embedded.class);
    }

    protected boolean isEmbeddable(Class<?> type) {
        return type.isAnnotationPresent(Embeddable.class);
    }

    protected boolean isIdProperty(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        if (annotatedProperty.getAnnotation(Id.class) != null) {
            return true;
        } else {
            return annotatedProperty.getAnnotation(EmbeddedId.class) != null;
        }
    }

    protected CascadeType[] getCascadeTypes(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            return manyToMany.cascade();
        }
        ManyToOne manyToOne = annotatedProperty.getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            return manyToOne.cascade();
        }
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            return oneToMany.cascade();
        }
        OneToOne oneToOne = annotatedProperty.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            return oneToOne.cascade();
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
}
