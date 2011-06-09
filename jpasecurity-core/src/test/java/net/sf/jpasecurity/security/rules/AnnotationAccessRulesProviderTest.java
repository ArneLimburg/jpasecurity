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
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;

import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.security.PermitWhere;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AnnotationAccessRulesProviderTest {

    private AnnotationAccessRulesProvider annotationAccessRulesProvider;

    @Before
    public void initializeProvider() {
        SecurityContext securityContext = createMock(SecurityContext.class);
        expect(securityContext.getAliases()).andReturn(Collections.singleton(new Alias("CURRENT_USER"))).anyTimes();
        replay(securityContext);
        annotationAccessRulesProvider = new AnnotationAccessRulesProvider();
        annotationAccessRulesProvider.setSecurityContext(securityContext);
        annotationAccessRulesProvider.setConfiguration(new Configuration());
    }

    @Test
    public void permitWhere() {
        Collection<String> permissions = annotationAccessRulesProvider.parsePermissions(Bean.class);
        assertEquals(1, permissions.size());
        assertEquals("GRANT CREATE READ UPDATE DELETE ACCESS TO " + Bean.class.getName() + " bean  "
                     + "WHERE bean.name = 'root' "
                     + "OR  EXISTS ( SELECT user FROM User user WHERE user = CURRENT_USER AND user.name = bean.name) ",
                     permissions.iterator().next());
    }

    @PermitWhere("name = 'root' OR EXISTS (SELECT user FROM User user WHERE user = CURRENT_USER AND user.name = name)")
    private static class Bean {
    }
}
