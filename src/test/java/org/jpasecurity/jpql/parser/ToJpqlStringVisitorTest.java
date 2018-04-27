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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

/** @author Arne Limburg */
public class ToJpqlStringVisitorTest {

    private static final Logger LOG = LoggerFactory.getLogger(ToStringVisitor.class);

    private JpqlParser parser;
    private ToJpqlStringVisitor toJpqlStringVisitor;

    @Test
    public void toStringVisitor() throws ParseException {
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean "
                + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean "
                + "WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE */ "
                + "bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean "
                + "WHERE NOT EXISTS ( SELECT /* IS_ACCESSIBLE_NODB */ bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean "
                + "WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB */ "
                + "bean FROM TestBean bean WHERE bean.id = :id)");
        assertJpql("SELECT bean FROM TestBean bean "
            + "WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)",
            "SELECT bean FROM TestBean bean "
                + "WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB"
                + " IS_ACCESSIBLE_NOCACHE*/ bean FROM TestBean bean WHERE bean.id = :id)");
    }

    public void assertJpql(String expected, String source) throws ParseException {
        StringBuilder queryBuilder = new StringBuilder();
        JpqlStatement statement = null;
        try {
            statement = parser.parseQuery(source);
        } catch (ParseException e) {
            fail("failed to parse jpql:\n\n" + source + "\n\n" + e.getMessage());
        }
        statement.visit(toJpqlStringVisitor, queryBuilder);
        source = stripWhiteSpaces(source);
        String result = stripWhiteSpaces(queryBuilder.toString());
        LOG.debug(source);
        LOG.debug(result);
        assertEquals("JPQL", expected, result);
    }

    protected String stripWhiteSpaces(String query) {
        return query.replaceAll("\\s+", " ").trim();
    }

    @Before
    public void initializeParser() throws ParseException {
        parser = new JpqlParser();
    }

    @Before
    public void initializeVisitor() {
        toJpqlStringVisitor = new ToJpqlStringVisitor();
    }
}
