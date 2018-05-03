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
package org.jpasecurity.jpql.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ToStringVisitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ToStringVisitor.class);

    private JpqlParser parser;
    private ToStringVisitor toStringVisitor;

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void toStringVisitor() throws ParseException {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT COUNT(bean) FROM TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT COUNT( DISTINCT bean.id) FROM TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT AVG(bean) FROM TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT SUM(bean) FROM TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT bean FROM org.jpasecurity.model.TestBean bean WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean LEFT OUTER JOIN bean.name name WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name beanName WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.name name WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN FETCH bean.name WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.parent parent WITH parent.name = 'Parent' "
                   + "WHERE bean.id = :id");
        assertJpql("SELECT bean FROM TestBean bean WHERE (bean.id BETWEEN 5 AND 7)");
        assertJpql("SELECT bean FROM TestBean bean WHERE (bean.id NOT BETWEEN 5 AND 7)");
        assertJpql("SELECT DISTINCT bean, bean.id FROM TestBean bean WHERE :id = bean.id");
        assertJpql("SELECT bean1 FROM TestBean bean1, TestBean bean2 WHERE bean1.id < bean2.id");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE (bean.name <> 'testBean')");
        assertJpql("SELECT bean1.name FROM TestBean bean1, TestBean bean2 WHERE (bean1.id - (bean2.id - 1)) = 5");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE bean.booleanValue = true");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) = 10.0");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) / 2) = 10.0");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE ((3 - 2) * 2) <= 10.0");
        assertJpql("SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) >= 10.0");
        assertJpql("SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 HAVING bean.id > 0");
        assertJpql("SELECT bean.id FROM TestBean bean "
                   + "WHERE ABS(bean.id) = 1 GROUP BY bean.id HAVING COUNT(bean.id) > 0");
        assertJpql("SELECT bean.id, bean.name, COUNT(bean.collectionProperty.id) "
                   + "FROM TestBean bean GROUP BY bean.id, bean.name");
        assertJpql("SELECT bean FROM TestBean bean "
                   + "WHERE TRIM(LEADING ' ' FROM bean.name) = TRIM(TRAILING FROM bean.name)");
        assertJpql("SELECT bean FROM TestBean bean WHERE TRIM(bean.name) = TRIM(BOTH FROM bean.name)");
        assertJpql("SELECT bean FROM TestBean bean "
                   + "WHERE bean.name = ALL( SELECT bean.collectionProperty.name FROM TestBean bean)");
        assertJpql("SELECT bean FROM TestBean bean "
                   + "WHERE bean.name = ANY( SELECT bean.collectionProperty.name FROM TestBean bean)");
        assertJpql("SELECT NEW org.jpasecurity.TestBean(bean.id, bean.name) FROM TestBean bean");
        assertJpql("SELECT bean FROM TestBean bean WHERE SIZE(bean.collectionProperty) = 0");
        assertJpql("SELECT DISTINCT bean FROM TestBean bean, TestBean bean2 INNER JOIN FETCH bean.collectionProperty");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.dateProperty = CURRENT_DATE");
        assertJpql("SELECT DISTINCT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.beanProperty");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.id > 0");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name LIKE '%beanName%'");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name LIKE '%beanName%' ESCAPE '\\'");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name NOT LIKE '%beanName%'");
        assertJpql("SELECT bean FROM TestBean bean "
                   + "WHERE (bean.collectionProperty IS NULL OR SIZE(bean.collectionProperty) = 0)");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name IS NOT NULL");
        assertJpql("SELECT bean FROM TestBean bean WHERE NOT (bean.id = SQRT(2) )");
        assertJpql("SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'test'");
        assertJpql("SELECT bean FROM TestBean bean ORDER BY bean.id ASC, bean.name DESC");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.id = MIN( DISTINCT bean.id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.id = MAX( DISTINCT bean.id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.id = MAX( DISTINCT bean.id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS EMPTY");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS NOT EMPTY");
        assertJpql("SELECT bean FROM TestBean bean WHERE (bean.id = 0 AND bean.name = 'Test')");
        assertJpql("SELECT bean FROM TestBean bean WHERE - MOD(bean.id, 2) = -1");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean MEMBER OF bean.collectionProperty");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean NOT MEMBER OF bean.collectionProperty");
        assertJpql("SELECT bean FROM TestBean bean WHERE EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
                   + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE */ bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT /* IS_ACCESSIBLE_NODB */ bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB IS_ACCESSIBLE_NOCACHE */"
            + " bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean WHERE SUBSTRING(bean.name, 2, 3) = 'est'");
        assertJpql("SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est') = 2");
        assertJpql("SELECT bean FROM TestBean bean WHERE CONCAT(bean.name, 'est') = 'Nameest'");
        assertJpql("SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est', 2) = -1");
        assertJpql("SELECT bean FROM TestBean bean WHERE LENGTH(bean.name) = 0");
        assertJpql("SELECT bean FROM TestBean bean WHERE UPPER(bean.name) = 'NAME'");
        assertJpql("SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'name'");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name = ?1");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIMESTAMP");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIME");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name IN ('name 1', 'name 2')");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')");
        assertJpql("SELECT bean, 'name', CASE WHEN bean.name = 'name 1' THEN bean.name ELSE 'name 2' END "
                   + "FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')");
        assertJpql("SELECT bean, 'name', CASE bean.name WHEN 'name 1' THEN bean.name ELSE 'name 2' END "
                   + "FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')");
        assertJpql("SELECT COALESCE(parent.name, KEY(related).name, VALUE(related).name, bean.name) "
                   + "FROM TestBean bean LEFT OUTER JOIN bean.parent parent LEFT OUTER JOIN bean.related related");
        assertJpql("SELECT NULLIF(bean.name, 'Test') FROM TestBean bean");
        assertJpql("SELECT bean, COUNT( DISTINCT bean) AS beanCount FROM TestBean bean WHERE bean.name = 'name 1'");
        assertJpql("SELECT bean FROM TestBean bean WHERE TYPE(bean) = TestBeanSubclass");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.related related "
                   + "WHERE KEY(related).name = 'name 1'");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.related related "
                   + "WHERE VALUE(related).name = 'name 1'");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.related related "
                   + "WHERE ENTRY(related) IS NOT NULL");
        assertJpql("SELECT bean FROM TestBean bean INNER JOIN bean.related related "
                   + "WHERE ENTRY(related) IS NULL");
        assertJpql("UPDATE TestBean bean SET bean.name = 'test', bean.id = 0");
        assertJpql("UPDATE TestBean bean SET bean.name = 'test', bean.id = 0 WHERE bean.id = 0");
        assertJpql("DELETE FROM TestBean bean");
        assertJpql("DELETE FROM TestBean bean WHERE bean.id = 0");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.value = ?1");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.key = ?1");
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.entry = ?1");
        assertAccessRule("GRANT CREATE READ UPDATE DELETE ACCESS TO TestBean bean WHERE bean.id = 0");
    }

    @Test
    public void parseWhereCoalesceInExpression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') IN ('Horst')");
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void  parseWhereCoalesceEquals1Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') = 'Horst'");
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void parseWhereCoalesceEquals2Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE 'Horst' = COALESCE(bean.name, 'Horst')");
    }

    @Test
    public void parseWhereCoalesceBetween1Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst')"
            + " BETWEEN bean.name AND bean.name");
    }

    @Test
    public void parseWhereCoalesceBetween2Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND bean.name");
    }
    @Test
    public void parseWhereCoalesceBetween3Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN"
            + " bean.name AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceBetween4Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND bean.name");
    }
    @Test
    public void parseWhereCoalesceBetween5Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN"
            + " bean.name AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceBetween6Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceBetween7Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')");
    }

    @Test
    public void parseWhereCoalesceNotBetween1Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN"
            + " bean.name AND bean.name");
    }

    @Test
    public void parseWhereCoalesceNotBetween2Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND bean.name");
    }
    @Test
    public void parseWhereCoalesceNotBetween3Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN"
            + " bean.name AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceNotBetween4Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND bean.name");
    }
    @Test
    public void parseWhereCoalesceNotBetween5Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN"
            + " bean.name AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceNotBetween6Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')");
    }
    @Test
    public void parseWhereCoalesceNotBetween7Expression() {
        assertJpql("SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN"
            + " COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')");
    }

    @Test
    public void parseWhereUpperInFunctions() {
        assertJpql("SELECT bean FROM TestBean bean WHERE UPPER(bean.name) IN ('Horst')");
    }

    @Test
    public void parseWhereUpperEqualsFunctions() {
        assertJpql("SELECT bean FROM TestBean bean WHERE UPPER(bean.name) = ('Horst')");
    }

    @Test
    public void parseSimpleJoin() {
        assertJpql("SELECT OBJECT(bean) FROM TestBean bean JOIN bean.beans");
    }

    private void assertJpql(String query) {
        StringBuilder queryBuilder = new StringBuilder();
        JpqlStatement statement = null;
        try {
            statement = parser.parseQuery(query);
        } catch (ParseException e) {
            fail("failed to parse jpql:\n\n" + query + "\n\n" + e.getMessage());
        }
        statement.visit(toStringVisitor, queryBuilder);
        query = stripWhiteSpaces(query);
        String result = stripWhiteSpaces(queryBuilder.toString());
        LOG.debug(query);
        LOG.debug(result);
        assertEquals("JPQL", query, result);
    }

    private void assertAccessRule(String rule) throws ParseException {
        StringBuilder ruleBuilder = new StringBuilder();
        JpqlAccessRule accessRule = parser.parseRule(rule);
        accessRule.visit(toStringVisitor, ruleBuilder);
        rule = stripWhiteSpaces(rule);
        String result = stripWhiteSpaces(ruleBuilder.toString());
        LOG.debug(rule);
        LOG.debug(result);
        assertEquals("AccessRule", rule, result);
    }

    private String stripWhiteSpaces(String query) {
        return query.replaceAll("\\s+", " ").trim();
    }

    @Before
    public void initializeParser() throws ParseException {
        parser = new JpqlParser();
    }

    @Before
    public void initializeVisitor() {
        toStringVisitor = new ToStringVisitor();
    }
}
