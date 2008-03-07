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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.Version;

/**
 * Parses classes for persistence annotations and stores mapping information
 * in the provided map.
 * @author Arne Limburg
 */
public class JpaAnnotationParser extends AbstractMappingParser {

    public JpaAnnotationParser(Map<Class<?>, ClassMappingInformation> classMappings) {
        super(classMappings);
    }

    protected Class<?> getTargetType(Member property) {
        AnnotatedElement annotatedProperty = (AnnotatedElement)property;
        OneToMany oneToMany = annotatedProperty.getAnnotation(OneToMany.class);
        if (oneToMany != null && oneToMany.targetEntity() != null) {
            return oneToMany.targetEntity();
        }
        ManyToMany manyToMany = annotatedProperty.getAnnotation(ManyToMany.class);
        if (manyToMany != null && manyToMany.targetEntity() != null) {
            return manyToMany.targetEntity();
        }
        return super.getTargetType(property);
    }

    protected boolean isMapped(Class<?> mappedClass) {
        return mappedClass.getAnnotation(Entity.class) != null
            || mappedClass.getAnnotation(Embeddable.class) != null
            || mappedClass.getAnnotation(MappedSuperclass.class) != null;
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
