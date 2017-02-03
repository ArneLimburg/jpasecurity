/*
 * Copyright 2011 - 2017 Arne Limburg
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
package org.jpasecurity.security.rules;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.jpasecurity.Path;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.ChildTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.model.ParentTestBean;
import org.jpasecurity.security.AccessRule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessRulesCompilerTest {

    @Test
    public void rulesOnInterfaces() {
        SecurityContext securityContext = createMock(SecurityContext.class);
        Metamodel metamodel = createMock(Metamodel.class);
        EntityType parentTestBeanType = createMock(EntityType.class);
        EntityType childTestBeanType = createMock(EntityType.class);
        EntityType methodAccessTestBeanType = createMock(EntityType.class);
        BasicType stringType = createMock(BasicType.class);
        SingularAttribute nameAttribute = createMock(SingularAttribute.class);
        expect(metamodel.getManagedTypes()).andReturn(new HashSet(Arrays.asList(
                parentTestBeanType, childTestBeanType, methodAccessTestBeanType))).anyTimes();
        expect(metamodel.getEntities()).andReturn(new HashSet(Arrays.asList(
                parentTestBeanType, childTestBeanType, methodAccessTestBeanType)));
        expect(metamodel.managedType(ParentTestBean.class)).andReturn(parentTestBeanType).anyTimes();
        expect(metamodel.managedType(ChildTestBean.class)).andReturn(childTestBeanType).anyTimes();
        expect(metamodel.managedType(MethodAccessTestBean.class)).andReturn(methodAccessTestBeanType).anyTimes();
        expect(parentTestBeanType.getName()).andReturn(ParentTestBean.class.getSimpleName()).anyTimes();
        expect(parentTestBeanType.getJavaType()).andReturn(ParentTestBean.class).anyTimes();
        expect(parentTestBeanType.getAttribute("name")).andReturn(nameAttribute);
        expect(childTestBeanType.getName()).andReturn(ChildTestBean.class.getSimpleName()).anyTimes();
        expect(childTestBeanType.getJavaType()).andReturn(ChildTestBean.class).anyTimes();
        expect(childTestBeanType.getAttribute("name")).andReturn(nameAttribute);
        expect(methodAccessTestBeanType.getName()).andReturn(MethodAccessTestBean.class.getSimpleName()).anyTimes();
        expect(methodAccessTestBeanType.getJavaType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("name")).andReturn(nameAttribute);
        expect(nameAttribute.getType()).andReturn(stringType).anyTimes();
        expect(nameAttribute.getJavaType()).andReturn(String.class).anyTimes();
        expect(stringType.getPersistenceType()).andReturn(PersistenceType.BASIC).anyTimes();
        replay(securityContext, metamodel);
        replay(parentTestBeanType, childTestBeanType, methodAccessTestBeanType, nameAttribute, stringType);
        AccessRulesParser parser
            = new AccessRulesParser("interface", metamodel, securityContext, new XmlAccessRulesProvider("interface"));
        assertEquals(2, parser.parseAccessRules().size());
    }

    @Test
    public void ruleWithSubselect() throws ParseException {
        String rule = " GRANT READ ACCESS TO ClientDetails cd "
                    + " WHERE cd.clientRelations.client.id "
                    + " IN (SELECT cs.client.id FROM ClientStaffing cs, ClientStatus cst, Employee e "
                    + "     WHERE e.email=CURRENT_PRINCIPAL AND cs.employee=e "
                    + "       AND cs.client= cd.clientRelation.client AND cs.endDate IS NULL "
                    + "       AND (cst.name <> 'Closed' OR cst.name IS NULL ))";
        Metamodel metamodel = createMock(Metamodel.class);
        EntityType clientTradeImportMonitorType = createMock(EntityType.class);
        expect(metamodel.getEntities()).andReturn(Collections.<EntityType<?>>singleton(clientTradeImportMonitorType));
        expect(clientTradeImportMonitorType.getName()).andReturn(ClientDetails.class.getSimpleName());
        expect(clientTradeImportMonitorType.getJavaType()).andReturn(ClientDetails.class);
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);

        replay(metamodel, clientTradeImportMonitorType);

        JpqlAccessRule accessRule = parser.parseRule(rule);
        Collection<AccessRule> compiledRules = compiler.compile(accessRule);
        assertThat(compiledRules.size(), is(1));
        AccessRule compiledRule = compiledRules.iterator().next();
        assertThat(compiledRule.getSelectedPath(), is(new Path("cd")));
    }

    private static class ClientDetails {
    }
}
