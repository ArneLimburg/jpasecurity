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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    public void rulesOnInterfaces() throws ParseException {
        SecurityContext securityContext = mock(SecurityContext.class);
        Metamodel metamodel = mock(Metamodel.class);
        EntityType parentTestBeanType = mock(EntityType.class);
        EntityType childTestBeanType = mock(EntityType.class);
        EntityType methodAccessTestBeanType = mock(EntityType.class);
        BasicType stringType = mock(BasicType.class);
        SingularAttribute nameAttribute = mock(SingularAttribute.class);
        when(metamodel.getManagedTypes()).thenReturn(new HashSet(Arrays.asList(
                parentTestBeanType, childTestBeanType, methodAccessTestBeanType)));
        when(metamodel.getEntities()).thenReturn(new HashSet(Arrays.asList(
                parentTestBeanType, childTestBeanType, methodAccessTestBeanType)));
        when(metamodel.managedType(ParentTestBean.class)).thenReturn(parentTestBeanType);
        when(metamodel.managedType(ChildTestBean.class)).thenReturn(childTestBeanType);
        when(metamodel.managedType(MethodAccessTestBean.class)).thenReturn(methodAccessTestBeanType);
        when(parentTestBeanType.getName()).thenReturn(ParentTestBean.class.getSimpleName());
        when(parentTestBeanType.getJavaType()).thenReturn(ParentTestBean.class);
        when(parentTestBeanType.getAttribute("name")).thenReturn(nameAttribute);
        when(childTestBeanType.getName()).thenReturn(ChildTestBean.class.getSimpleName());
        when(childTestBeanType.getJavaType()).thenReturn(ChildTestBean.class);
        when(childTestBeanType.getAttribute("name")).thenReturn(nameAttribute);
        when(methodAccessTestBeanType.getName()).thenReturn(MethodAccessTestBean.class.getSimpleName());
        when(methodAccessTestBeanType.getJavaType()).thenReturn(MethodAccessTestBean.class);
        when(methodAccessTestBeanType.getAttribute("name")).thenReturn(nameAttribute);
        when(nameAttribute.getType()).thenReturn(stringType);
        when(nameAttribute.getJavaType()).thenReturn(String.class);
        when(stringType.getPersistenceType()).thenReturn(PersistenceType.BASIC);
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
        Metamodel metamodel = mock(Metamodel.class);
        EntityType clientTradeImportMonitorType = mock(EntityType.class);
        when(metamodel.getEntities()).thenReturn(Collections.<EntityType<?>>singleton(clientTradeImportMonitorType));
        when(clientTradeImportMonitorType.getName()).thenReturn(ClientDetails.class.getSimpleName());
        when(clientTradeImportMonitorType.getJavaType()).thenReturn(ClientDetails.class);
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);

        JpqlAccessRule accessRule = parser.parseRule(rule);
        Collection<AccessRule> compiledRules = compiler.compile(accessRule);
        assertThat(compiledRules.size(), is(1));
        AccessRule compiledRule = compiledRules.iterator().next();
        assertThat(compiledRule.getSelectedPath(), is(new Path("cd")));
    }

    private static class ClientDetails {
    }
}
