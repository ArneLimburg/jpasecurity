/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManagerFactory;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NamedQueryParserTest {

    private static final int ALL_NAMED_QUERY_COUNT = 9;
    private static final int NAMED_QUERY_COUNT = 6;
    private EntityManagerFactory entityManagerFactory;
    private NamedQueryParser parser;

    @Before
    public void createParser() {
        entityManagerFactory = new HibernatePersistenceProvider().createEntityManagerFactory("metamodel", null);
        Set<String> ormXmlLocations = new HashSet<>();
        ormXmlLocations.add("META-INF/all.orm.xml");
        ormXmlLocations.add("META-INF/empty.orm.xml");
        ormXmlLocations.add("META-INF/parent.orm.xml");
        parser = new NamedQueryParser(entityManagerFactory.getMetamodel(), ormXmlLocations);
    }

    @After
    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    @Test
    public void parseNamedQueries() {
        ConcurrentMap<String, String> namedQueries = parser.parseNamedQueries();
        assertThat(namedQueries.size(), is(ALL_NAMED_QUERY_COUNT));
        assertThat(namedQueries.get("MethodAccessTestBean.findAll"),
                is("SELECT m FROM MethodAccessTestBean m"));
        assertThat(namedQueries.get("MethodAccessTestBean.findById"),
                is("SELECT m FROM MethodAccessTestBean m WHERE m.id = :id"));
        assertThat(namedQueries.get("MethodAccessTestBean.findByName"),
                is("SELECT m FROM MethodAccessTestBean m WHERE m.name = :name"));
        assertThat(namedQueries.get("ParentTestBean.findAll"),
                is("SELECT p FROM ParentTestBean p"));
        assertThat(namedQueries.get("ParentTestBean.findById"),
                is("SELECT p FROM ParentTestBean p WHERE p.id = :id"));
        assertThat(namedQueries.get("ParentTestBean.findByName"),
                is("SELECT p FROM ParentTestBean p WHERE p.name = :name"));
        assertThat(namedQueries.get("ChildTestBean.findAll"),
                is("SELECT c FROM ChildTestBean c"));
        assertThat(namedQueries.get("ChildTestBean.findById"),
                is("SELECT c FROM ChildTestBean c WHERE c.id = :id"));
        assertThat(namedQueries.get("ChildTestBean.findByName"),
                is("SELECT c FROM ChildTestBean c WHERE c.name = :name"));
    }

    @Test
    public void parseNamedQueryInOrmXml() {
        parser = new NamedQueryParser(
                entityManagerFactory.getMetamodel(), Collections.singleton("META-INF/named-query.xml"));
        Map<String, String> namedQueries = parser.parseNamedQueries();

        assertEquals(NAMED_QUERY_COUNT, namedQueries.size());
        assertEquals("select test from Contact test", namedQueries.get("myQuery1"));
    }

    @Test
    public void parseNamedQueriesInOrmXml() {
        parser = new NamedQueryParser(
                entityManagerFactory.getMetamodel(), Collections.singleton("META-INF/named-queries.xml"));
        Map<String, String> namedQueries = parser.parseNamedQueries();

        assertEquals(ALL_NAMED_QUERY_COUNT, namedQueries.size());

        assertEquals("select test from Contact test", namedQueries.get("myQuery1"));
        assertEquals("select test from Contact test", namedQueries.get("myQuery2"));
        assertEquals("select test from Contact test", namedQueries.get("myQuery3"));
        assertEquals("select test from Contact test", namedQueries.get("myQuery4"));
    }
}
