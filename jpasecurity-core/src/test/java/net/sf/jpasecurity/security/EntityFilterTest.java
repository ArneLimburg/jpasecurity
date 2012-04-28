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
package net.sf.jpasecurity.security;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.model.MethodAccessTestBean;
import net.sf.jpasecurity.security.rules.AccessRulesCompiler;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EntityFilterTest {

    private static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    private static final String NAME = "JUnit";
    private EntityFilter entityFilter;

    @Before
    public void initialize() throws ParseException {
        SecureObjectCache objectCache = createMock(SecureObjectCache.class);
        MappingInformation mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        PropertyMappingInformation idPropertyMapping = createMock(PropertyMappingInformation.class);
        PropertyMappingInformation namePropertyMapping = createMock(PropertyMappingInformation.class);
        SecurityContext securityContext = createMock(SecurityContext.class);
        String className = MethodAccessTestBean.class.getSimpleName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(className)).andReturn(classMapping).anyTimes();
        expect(mappingInformation.getClassMapping(MethodAccessTestBean.class)).andReturn(classMapping).anyTimes();
        expect(mappingInformation.<Object>getType((Alias)anyObject(), (Set<TypeDefinition>)anyObject()))
            .andAnswer(new TypeAnswer<Object>()).anyTimes();
        expect(mappingInformation.<Object>getType((Path)anyObject(), (Set<TypeDefinition>)anyObject()))
            .andAnswer(new TypeAnswer<Object>()).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("tb.id")), (Set<TypeDefinition>)anyObject()))
            .andReturn(idPropertyMapping).anyTimes();
        expect(classMapping.<MethodAccessTestBean>getEntityType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(classMapping.getEntityName()).andReturn(Introspector.decapitalize(className)).anyTimes();
        expect(classMapping.containsPropertyMapping("name")).andReturn(true).anyTimes();
        expect(classMapping.getPropertyMapping("name")).andReturn(namePropertyMapping).anyTimes();
        expect(idPropertyMapping.isRelationshipMapping()).andReturn(false);
        expect(namePropertyMapping.getPropertyValue(anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return ((MethodAccessTestBean)getCurrentArguments()[0]).getName();
            }
        }).anyTimes();
        expect(securityContext.getAliases()).andReturn(Collections.singleton(CURRENT_PRINCIPAL));
        expect(securityContext.getAliasValue(CURRENT_PRINCIPAL)).andReturn(NAME);
        replay(objectCache, mappingInformation, classMapping, idPropertyMapping, namePropertyMapping, securityContext);
        entityFilter = new EntityFilter(objectCache,
                                        mappingInformation,
                                        securityContext,
                                        new DefaultExceptionFactory(),
                                        initializeAccessRules(mappingInformation));
    }

    private List<AccessRule> initializeAccessRules(MappingInformation mappingInformation) throws ParseException {
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation);
        String rule = "GRANT READ ACCESS TO MethodAccessTestBean testBean WHERE testBean.name = CURRENT_PRINCIPAL";
        JpqlAccessRule parsedRule = parser.parseRule(rule);
        return new ArrayList<AccessRule>(compiler.compile(parsedRule));
    }

    @Test
    public void filterQuery() {
        String plainQuery = "SELECT tb FROM MethodAccessTestBean tb";
        String restrictedQuery = "SELECT tb FROM MethodAccessTestBean tb WHERE (tb.name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
        Map<String, Object> parameters = result.getParameters();
        assertEquals(1, parameters.size());
        Map.Entry<String, Object> parameter = parameters.entrySet().iterator().next();
        assertEquals(CURRENT_PRINCIPAL.getName(), parameter.getKey());
        assertEquals(NAME, parameter.getValue());
    }

    @Test
    public void filterQueryWithSimpleSelectedType() {
        String plainQuery = "SELECT tb.id FROM MethodAccessTestBean tb";
        String restrictedQuery = "SELECT tb.id FROM MethodAccessTestBean tb WHERE (tb.name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void isAccessible() throws NotEvaluatableException {
        MethodAccessTestBean testBean = new MethodAccessTestBean();
        testBean.setName(NAME);
        assertTrue(entityFilter.isAccessible(testBean, AccessType.READ));
        assertFalse(entityFilter.isAccessible(testBean, AccessType.UPDATE));
    }

    private static class TypeAnswer<T> implements IAnswer<Class<T>> {

        public Class<T> answer() throws Throwable {
            Path path = new Path(getCurrentArguments()[0].toString());
            Set<TypeDefinition> typeDefinitions = (Set<TypeDefinition>)getCurrentArguments()[1];
            for (TypeDefinition typeDefinition: typeDefinitions) {
                if (typeDefinition.getAlias().getName().equals(path.getRootAlias().getName())) {
                    if (path.getSubpath() == null) {
                        return (Class<T>)typeDefinition.getType();
                    } else if (path.getSubpath().equals("id")) {
                        return (Class<T>)Integer.class;
                    }
                }
            }
            return null;
        }
    }
}
