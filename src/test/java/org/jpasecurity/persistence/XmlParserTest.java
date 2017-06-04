/*
 * Copyright 2016 - 2016 Arne Limburg
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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class XmlParserTest {

    private XmlParser parser;

    @Before
    public void initializeParser() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        parser = new XmlParser("anotherFile.xml",
                               "missingFile.xml",
                               "META-INF/persistence.xml",
                               "META-INF/empty.orm.xml",
                               "META-INF/all.orm.xml",
                               "META-INF/parent.orm.xml");
    }

    @Test
    public void parsePersistenceProviderReturnsNullForEmptyPersistenceProvider() throws XPathExpressionException {
        assertThat(parser.parsePersistenceProvider("unit-test"), is(nullValue()));
    }

    @Test
    public void parsePersistenceProviderFindsSecurePersistenceProvider() throws XPathExpressionException {
        assertThat(parser.parsePersistenceProvider("integration-test"), is(SecurePersistenceProvider.class.getName()));
    }

    @Test
    public void parseMappingFiles() throws XPathExpressionException {
        Set<String> mappingFiles = parser.parseMappingFileNames("unit-test");
        assertThat(mappingFiles.size(), is(3));
        assertThat(mappingFiles, hasItem("security.orm.xml"));
        assertThat(mappingFiles, hasItem("META-INF/empty.orm.xml"));
        assertThat(mappingFiles, hasItem("META-INF/parent.orm.xml"));
    }

    @Test
    public void parsePersistencePropertyFindsMockPersistenceProvider() throws XPathExpressionException {
        assertThat(parser.parsePersistenceProperty("integration-test", "org.jpasecurity.persistence.provider"),
                is(MockitoPersistenceProvider.class.getName()));
    }

    @Test
    public void parseGlobalNamedQueryFindsGlobalQueries() throws XPathExpressionException {
        Set<Node> nodes = parser.parseGlobalNamedQueries();
        assertThat(nodes.size(), is(2));
        Iterator<Node> i = nodes.iterator();
        Node node1 = i.next();
        Node node2 = i.next();
        Map<String, String> namedQueries = new HashMap<String, String>();
        namedQueries.put(node1.getParentNode().getAttributes().getNamedItem("name").getTextContent(),
                node1.getTextContent());
        namedQueries.put(node2.getParentNode().getAttributes().getNamedItem("name").getTextContent(),
                node2.getTextContent());
        assertThat(namedQueries.get("ParentTestBean.findAll"), is("SELECT p FROM ParentTestBean p"));
        assertThat(namedQueries.get("ParentTestBean.findById"), is("SELECT p FROM ParentTestBean p WHERE p.id = :id"));
    }

    @Test
    public void parseEntityNamedQueryFindsEntityQueries() throws Exception {
        Set<Node> nodes = parser.parseEntityNamedQueries();
        assertThat(nodes.size(), is(2));
        Iterator<Node> i = nodes.iterator();
        Node node1 = i.next();
        Node node2 = i.next();
        Map<String, String> namedQueries = new HashMap<String, String>();
        namedQueries.put(node1.getParentNode().getAttributes().getNamedItem("name").getTextContent(),
                node1.getTextContent());
        namedQueries.put(node2.getParentNode().getAttributes().getNamedItem("name").getTextContent(),
                node2.getTextContent());
        assertThat(namedQueries.get("findAll"), is("SELECT m FROM MethodAccessTestBean m"));
        assertThat(namedQueries.get("findByName"), is("SELECT p FROM ParentTestBean p WHERE p.name = :name"));
    }
}
