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

import static org.junit.Assert.fail;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.jpa.JpaSecurityUnit;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessXmlTestBean;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.model.TestEntityListener.PackageProtectedTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.PrivateTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.ProtectedTestMethodCalledException;
import net.sf.jpasecurity.model.TestEntityListener.PublicTestMethodCalledException;
import net.sf.jpasecurity.persistence.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EntityListenerTest {

    @Test
    public void parseEntityListenerAnnotations() {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(MethodAccessAnnotationTestBean.class.getName());
        SecurityUnit securityUnitInformation = new JpaSecurityUnit(persistenceUnitInfo);
        MappingInformation mappingInformation
            = new JpaAnnotationParser(new JpaExceptionFactory()).parse(securityUnitInformation);
        try {
            mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).prePersist(null);
            fail("expected call to publicTestMethod");
        } catch (PublicTestMethodCalledException e) {
            //expected
        }
        mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).postPersist(null);
        try {
            mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).preRemove(null);
            fail("expected call to protectedTestMethod");
        } catch (ProtectedTestMethodCalledException e) {
            //expected
        }
        mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).postRemove(null);
        try {
            mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).preUpdate(null);
            fail("expected call to packageProtectedTestMethod");
        } catch (PackageProtectedTestMethodCalledException e) {
            //expected
        }
        mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).postUpdate(null);
        mappingInformation.getClassMapping(MethodAccessAnnotationTestBean.class).postLoad(null);
    }

    @Test
    public void xmlEntityListeners() {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        SecurityUnit securityUnitInformation = new JpaSecurityUnit(persistenceUnitInfo);
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        ExceptionFactory exceptionFactory = new JpaExceptionFactory();
        MappingInformation mappingInformation
            = new JpaAnnotationParser(exceptionFactory).parse(securityUnitInformation);
        mappingInformation = new OrmXmlParser(exceptionFactory).parse(securityUnitInformation, mappingInformation);
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
            fail("expected call to protectedTestMethod");
        } catch (ProtectedTestMethodCalledException e) {
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
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postUpdate(null);
            fail("expected call to packageProtectedTestMethod");
        } catch (PackageProtectedTestMethodCalledException e) {
            //expected
        }
        try {
            mappingInformation.getClassMapping(FieldAccessXmlTestBean.class).postLoad(null);
            fail("expected call to privateTestMethod");
        } catch (PrivateTestMethodCalledException e) {
            //expected
        }
    }
}
