/*
 * Copyright 2011 Arne Limburg
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

import java.util.Collection;

import org.jpasecurity.DefaultSecurityUnit;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.configuration.AccessRule;
import org.jpasecurity.configuration.Configuration;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.Path;
import org.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import org.jpasecurity.model.ChildTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.model.ParentTestBean;
import org.jpasecurity.security.authentication.AutodetectingSecurityContext;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessRulesCompilerTest {

    @Test
    @Ignore("This test is bogus")
    public void rulesOnInterfaces() {
        SecurityUnit securityUnit = new DefaultSecurityUnit("interface");
        securityUnit.getManagedClassNames().add(ParentTestBean.class.getName());
        securityUnit.getManagedClassNames().add(ChildTestBean.class.getName());
        securityUnit.getManagedClassNames().add(MethodAccessTestBean.class.getName());
        MappingInformation mappingInformation = new JavaBeanSecurityUnitParser(securityUnit).parse();
        XmlAccessRulesProvider accessRulesProvider = new XmlAccessRulesProvider();
        accessRulesProvider.setMappingInformation(mappingInformation);
        accessRulesProvider.setConfiguration(new Configuration());
        accessRulesProvider.setSecurityContext(new AutodetectingSecurityContext());
        assertEquals(2, accessRulesProvider.getAccessRules().size());
    }

    @Test
    public void ruleWithSubselect() throws ParseException {
        String rule = " GRANT READ ACCESS TO ClientDetails cd "
                    + " WHERE cd.clientRelations.client.id "
                    + " IN (SELECT cs.client.id FROM ClientStaffing cs, ClientStatus cst, Employee e "
                    + "     WHERE e.email=CURRENT_PRINCIPAL AND cs.employee=e "
                    + "       AND cs.client= cd.clientRelation.client AND cs.endDate IS NULL "
                    + "       AND (cst.name <> 'Closed' OR cst.name IS NULL ))";
        MappingInformation mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation clientTradeImportMonitorMapping = createMock(ClassMappingInformation.class);
        expect(mappingInformation.containsClassMapping("ClientDetails")).andReturn(true);
        expect(mappingInformation.getClassMapping("ClientDetails")).andReturn(clientTradeImportMonitorMapping);
        expect(clientTradeImportMonitorMapping.<ClientDetails>getEntityType())
            .andReturn(ClientDetails.class).anyTimes();
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation);

        replay(mappingInformation, clientTradeImportMonitorMapping);

        JpqlAccessRule accessRule = parser.parseRule(rule);
        Collection<AccessRule> compiledRules = compiler.compile(accessRule);
        assertThat(compiledRules.size(), is(1));
        AccessRule compiledRule = compiledRules.iterator().next();
        assertThat(compiledRule.getSelectedPath(), is(new Path("cd")));
    }

    private static class ClientDetails {
    }
}
