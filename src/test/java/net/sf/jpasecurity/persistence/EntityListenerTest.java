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
package net.sf.jpasecurity.persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.OrmXmlParser;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessXmlTestBean;
import net.sf.jpasecurity.model.TestEntityListener.PackageProtectedTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.PrivateTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.ProtectedTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.PublicTestMethodCalledException;

/**
 * @author Arne Limburg
 */
public class EntityListenerTest extends TestCase {

    public void testParseEntityListenerAnnotations() {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        MappingInformation mappingInformation = new JpaAnnotationParser().parse(persistenceUnitInfo);
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).prePersist(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).postPersist(null);
            fail("expected call to packageProtectedTestMethod");
        } catch (PackageProtectedTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).preRemove(null);
            fail("expected call to privateTestMethod");
        } catch (PrivateTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).postRemove(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).preUpdate(null);
            fail("expected call to packageProtectedTestMethod");
        } catch (PackageProtectedTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).postUpdate(null);
            fail("expected call to protectedTestMethod");
        } catch (ProtectedTestMethodCalledException e) {
            //expected
        }
        mappingInformation.getClassMapping(FieldAccessAnnotationTestBean.class).postLoad(null);
    }
    
    public void testXmlEntityListeners() {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        MappingInformation mappingInformation = new JpaAnnotationParser().parse(persistenceUnitInfo);
        mappingInformation = new OrmXmlParser().parse(persistenceUnitInfo, mappingInformation);
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).prePersist(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postPersist(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).preRemove(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postRemove(null);
            fail("expected call to protectedTestMethod");
        } catch (ProtectedTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).preUpdate(null);
            fail("expected call to packageProtectedTestMethod");
        } catch (PackageProtectedTestMethodCalledException e) {
            //expected
        }
        mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postUpdate(null);
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postLoad(null);
            fail("expected call to privateTestMethod");
        } catch (PrivateTestMethodCalledException e) {
            //expected
        }
    }
}
