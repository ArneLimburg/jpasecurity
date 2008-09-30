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
package net.sf.jpasecurity.compiler;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.jpql.compiler.EntityFilter;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.mapping.TestMappingInformation;
import net.sf.jpasecurity.security.rules.AccessRule;
import net.sf.jpasecurity.security.rules.AccessType;

/**
 * @author Arne Limburg
 */
public class EntityFilterTest extends TestCase {

    private MappingInformation mappingInformation;
    private List<AccessRule> accessRules;
    
    public void setUp() throws Exception {
        mappingInformation = new TestMappingInformation(Contact.class, User.class);
        JpqlParser parser = new JpqlParser();
        JpqlAccessRule rule
            = parser.parseRule("GRANT READ ACCESS TO Contact contact WHERE contact.owner = CURRENT_USER");
        JpqlCompiler compiler = new JpqlCompiler(mappingInformation);
        accessRules = Collections.singletonList(compiler.compile(rule));
    }
    
    public void testIsAccessible() throws Exception{
        EntityFilter filter = new EntityFilter(null, mappingInformation, accessRules);
        User john = new User("John");
        Contact contact = new Contact(john, "123456789");
        assertTrue(filter.isAccessible(contact, AccessType.READ, john, Collections.EMPTY_SET));
        User mary = new User("Mary");
        assertFalse(filter.isAccessible(contact, AccessType.READ, mary, Collections.EMPTY_SET));
    }
}
