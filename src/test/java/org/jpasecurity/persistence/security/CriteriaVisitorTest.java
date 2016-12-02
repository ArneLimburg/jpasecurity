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
package org.jpasecurity.persistence.security;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jpasecurity.configuration.AccessRule;
import org.jpasecurity.configuration.SecurityContext;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.mapping.Alias;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.Path;
import org.jpasecurity.mapping.TypeDefinition;
import org.jpasecurity.model.TestBean;
import org.jpasecurity.persistence.JpaExceptionFactory;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaVisitorTest {

    private MappingInformation mappingInformation;
    private SecurityContext securityContext;
    private JpqlParser parser;
    private AccessRulesCompiler compiler;
    private CriteriaVisitor criteriaVisitor;
    private EntityManagerFactory entityManagerFactory;
    private TestBean bean1;
    private TestBean bean2;

    @Before
    public void initialize() {
        mappingInformation = createMock(MappingInformation.class);
        expect(mappingInformation.containsClassMapping("TestBean")).andReturn(true).anyTimes();
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        expect(mappingInformation.getClassMapping("TestBean")).andReturn(classMapping).anyTimes();
        expect(mappingInformation.<TestBean>getType((Path)anyObject(), (Set<TypeDefinition>)anyObject()))
            .andReturn(TestBean.class);
        expect(mappingInformation.<TestBean>getType((Alias)anyObject(), (Set<TypeDefinition>)anyObject()))
            .andReturn(TestBean.class);
        expect(classMapping.<TestBean>getEntityType()).andReturn(TestBean.class).anyTimes();
        securityContext = createMock(SecurityContext.class);
        replay(mappingInformation, classMapping, securityContext);
        parser = new JpqlParser();
        compiler = new AccessRulesCompiler(mappingInformation, new JpaExceptionFactory());
        entityManagerFactory = Persistence.createEntityManagerFactory("hibernate");
        criteriaVisitor = new CriteriaVisitor(mappingInformation,
                                              entityManagerFactory.getCriteriaBuilder(),
                                              securityContext);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        bean1 = new TestBean();
        bean2 = new TestBean();
        entityManager.persist(bean1);
        entityManager.persist(bean2);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void appendAccessRule() {
        AccessRule accessRule = compile("GRANT READ ACCESS TO TestBean testBean WHERE testBean.id = 1");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TestBean> query = criteriaBuilder.createQuery(TestBean.class);
        Root<TestBean> from = query.from(TestBean.class);
        from.alias("testBean");
        query.where(from.get("parent").isNull());

        accessRule.getWhereClause().visit(criteriaVisitor, new CriteriaHolder(query));
        List<TestBean> result = entityManager.createQuery(query).getResultList();
        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().getId());
    }

    private AccessRule compile(String accessRule) {
        try {
            Collection<AccessRule> accessRules = compiler.compile(parser.parseRule(accessRule));
            assertEquals(1, accessRules.size());
            return accessRules.iterator().next();
        } catch (ParseException e) {
            throw new IllegalArgumentException(accessRule, e);
        }
    }
}
