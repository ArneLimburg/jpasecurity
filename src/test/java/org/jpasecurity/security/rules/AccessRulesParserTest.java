/*
 * Copyright 2018 Arne Limburg
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

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.Permit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessRulesParserTest {

    @Mock
    private Metamodel metamodel;
    @Mock
    private ManagedType<?> managedType;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private AccessRulesProvider accessRulesProvider;

    private AccessRulesParser parser;

    @Before
    public void createAccessRulesParser() {
        when(metamodel.getManagedTypes()).thenReturn((Set)singleton(managedType));
        parser = new AccessRulesParser("test", metamodel, securityContext, accessRulesProvider);
    }

    @Test
    public void subselectWithWithClause() {
        when(managedType.getJavaType()).thenReturn((Class)RuleWithSubselectAndWith.class);
        Collection<AccessRule> accessRules = parser.parseAccessRules();
        assertThat(accessRules, hasSize(1));
        assertThat(accessRules.iterator().next().getStatement().toString(),
            is(" GRANT  CREATE  READ  UPDATE  DELETE ACCESS TO"
                + " org.jpasecurity.security.rules.AccessRulesParserTest$RuleWithSubselectAndWith"
                + " ruleWithSubselectAndWith"
                + " WHERE  EXISTS ( SELECT b FROM RuleWithSubselectAndWith b"
                + " LEFT OUTER JOIN b.parent p  WITH p IS NOT NULL  ) "));
    }

    @Permit(where = "EXISTS (SELECT b FROM RuleWithSubselectAndWith b LEFT JOIN b.parent p WITH p IS NOT NULL)")
    public static class RuleWithSubselectAndWith {

    }
}
