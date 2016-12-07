/*
 * Copyright 2016 Arne Limburg
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlParser {

    private static final String PERSISTENCE_PROVIDER_XPATH = "/*[local-name()=''persistence'']"
        + "/*[local-name()=''persistence-unit'' and @name=''{0}'']/*[local-name()=''provider'']/text()";
    private static final String MAPPING_FILE_XPATH = "/*[local-name()=''persistence'']"
        + "/*[local-name()=''persistence-unit'' and @name=''{0}'']/*[local-name()=''mapping-file'']/text()";
    private static final String PERSISTENCE_PROPERTY_XPATH = "/*[local-name()=''persistence'']"
        + "/*[local-name()=''persistence-unit'' and @name=''{0}'']/*[local-name()=''properties'']"
        + "/*[local-name()=''property'' and @name=''{1}'']/@value";
    private static final String GLOBAL_NAMED_QUERY_XPATH = "/*[local-name()='entity-mappings']"
        + "/*[local-name()='named-query']/*[local-name()='query']";
    private static final String ENTITY_NAMED_QUERY_XPATH = "/*[local-name()='entity-mappings']"
        + "/*[local-name()='entity']/*[local-name()='named-query']/*[local-name()='query']";

    private DocumentBuilder builder;
    private XPath xpath;
    private List<Document> documents;

    public XmlParser(String... resourceNames)
        throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        this(getResources(resourceNames));
    }

    public XmlParser(Collection<URL> documentUrls)
        throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(false);
        xpath = XPathFactory.newInstance().newXPath();
        builder = documentBuilderFactory.newDocumentBuilder();
        documents = loadDocuments(documentUrls);
    }

    public String parsePersistenceProvider(String name) throws XPathExpressionException {
        return parseValue(MessageFormat.format(PERSISTENCE_PROVIDER_XPATH, name));
    }

    public String parsePersistenceProperty(String unitName, String propertyName) throws XPathExpressionException {
        return parseValue(MessageFormat.format(PERSISTENCE_PROPERTY_XPATH, unitName, propertyName));
    }

    public Set<String> parseMappingFileNames(String unitName) throws XPathExpressionException {
        return parseValues(MessageFormat.format(MAPPING_FILE_XPATH, unitName));
    }

    public Set<Node> parseGlobalNamedQueries() throws XPathExpressionException {
        return parseNodeSet(GLOBAL_NAMED_QUERY_XPATH);
    }

    public Set<Node> parseEntityNamedQueries() throws XPathExpressionException {
        return parseNodeSet(ENTITY_NAMED_QUERY_XPATH);
    }

    private String parseValue(String xpathExpression) throws XPathExpressionException {
        XPathExpression expression = xpath.compile(xpathExpression);
        for (Document document: documents) {
            String result = (String)expression.evaluate(document, XPathConstants.STRING);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        }
        return null;
    }

    private Set<String> parseValues(String xpathExpression) throws XPathExpressionException {
        Set<String> result = new HashSet<String>();
        XPathExpression expression = xpath.compile(xpathExpression);
        for (Document document: documents) {
            NodeList values = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < values.getLength(); i++) {
                result.add(values.item(i).getTextContent());
            }
        }
        return result;
    }

    private Set<Node> parseNodeSet(String xpathExpression) throws XPathExpressionException {
        Set<Node> nodeSet = new HashSet<Node>();
        XPathExpression expression = xpath.compile(xpathExpression);
        for (Document document: documents) {
            NodeList result = (NodeList)expression.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                nodeSet.add(result.item(i));
            }
        }
        return nodeSet;
    }

    private static Collection<URL> getResources(String... resourceNames) throws IOException {
        List<URL> resources = new ArrayList<URL>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String resourceName: resourceNames) {
            resources.addAll(Collections.list(classLoader.getResources(resourceName)));
        }
        return resources;
    }

    private List<Document> loadDocuments(Collection<URL> urls) throws SAXException, IOException, URISyntaxException {
        List<Document> documents = new ArrayList<Document>();
        for (URL documentUrl: urls) {
            Document document = builder.parse(documentUrl.toURI().toString());
            documents.add(document);
        }
        return documents;
    }
}
