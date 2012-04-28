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
package net.sf.jpasecurity.security.rules;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import net.sf.jpasecurity.DefaultSecurityUnit;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import net.sf.jpasecurity.model.ChildTestBean;
import net.sf.jpasecurity.model.MethodAccessTestBean;
import net.sf.jpasecurity.model.ParentTestBean;
import net.sf.jpasecurity.security.authentication.AutodetectingSecurityContext;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessRulesCompilerTest {

    @Test
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
