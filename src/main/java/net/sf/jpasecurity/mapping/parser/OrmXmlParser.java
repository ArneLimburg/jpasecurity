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
package net.sf.jpasecurity.mapping.parser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.jpasecurity.xml.XmlNodeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Parser to parse orm.xml
 * <strong>This class is not thread-safe</strong>
 * @todo support <xml-mapping-metadata-complete/> tag
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class OrmXmlParser extends AbstractMappingParser {

    private static final String NAMED_QUERY_XPATH = "//named-query";
    private static final String CASCADE_TYPE_XPATH = "//entity[@class=''{0}'']//*[@name=''{1}'']/cascade/*";

    private static final Log LOG = LogFactory.getLog(OrmXmlParser.class);

    /**
     * The tag name for entities
     */
    public static final String ENTITY_TAG_NAME = "entity";

    /**
     * The tag name for mapped superclasses
     */
    public static final String MAPPED_SUPERCLASS_TAG_NAME = "mapped-superclass";

    /**
     * The tag name for embeddables
     */
    public static final String EMBEDDABLE_TAG_NAME = "embeddable";

    /**
     * The tag name for attributes
     */
    public static final String ATTRIBUTES_TAG_NAME = "attributes";

    /**
     * The tag name for id classes
     */
    public static final String ID_CLASS_TAG_NAME = "id-class";

    /**
     * The tag name for embedded ids
     */
    public static final String EMBEDDED_ID_TAG_NAME = "embedded-id";

    /**
     * The tag name for ids
     */
    public static final String ID_TAG_NAME = "id";

    /**
     * The tag name for many-to-one mappings
     */
    public static final String MANY_TO_ONE_TAG_NAME = "many-to-one";

    /**
     * The tag name for one-to-one mappings
     */
    public static final String ONE_TO_ONE_TAG_NAME = "one-to-one";

    /**
     * The tag name for one-to-many mappings
     */
    public static final String ONE_TO_MANY_TAG_NAME = "one-to-many";

    /**
     * The tag name for many-to-many mappings
     */
    public static final String MANY_TO_MANY_TAG_NAME = "many-to-many";
    
    /**
     * The tag name for transient attributes
     */
    public static final String TRANSIENT_TAG_NAME = "transient";

    /**
     * The attribute name for classes
     */
    public static final String CLASS_ATTRIBUTE_NAME = "class";

    /**
     * The attribute name for names
     */
    public static final String NAME_ATTRIBUTE_NAME = "name";

    /**
     * The attribute name for access
     */
    public static final String ACCESS_ATTRIBUTE_NAME = "access";

    /**
     * The field access type value
     */
    public static final String FIELD_ACCESS = "FIELD";

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();;

    private XmlNodeList entityNodes;
    private XmlNodeList superclassNodes;
    private XmlNodeList embeddableNodes;
    private Document ormDocument;

    public void parsePersistenceUnit(PersistenceUnitInfo persistenceUnit) {
        parse(persistenceUnit, "META-INF/orm.xml");
        for (String mappingFilename: persistenceUnit.getMappingFileNames()) {
            parse(persistenceUnit, mappingFilename);
        }        
    }
    
    protected Class<?> getIdClass(Class<?> entityClass, boolean useFieldAccess) {
        Node entityNode = getEntityNode(entityClass);
        XmlNodeList childNodes = new XmlNodeList(entityNode.getChildNodes());
        List<Node> idClassNodes = childNodes.subList(ID_CLASS_TAG_NAME, "");
        if (idClassNodes.size() > 0) {
            try {
                String className
                    = idClassNodes.get(0).getAttributes().getNamedItem(CLASS_ATTRIBUTE_NAME).getTextContent();
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new PersistenceException(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isEmbeddable(Class<?> type) {
        return embeddableNodes.containsAttribute(CLASS_ATTRIBUTE_NAME, type.getName());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Node entityNode = getEntityNode(mappedClass);
        NamedNodeMap attributes = entityNode.getAttributes();
        if (attributes == null) {
            return super.usesFieldAccess(mappedClass);
        }
        Node access = attributes.getNamedItem(ACCESS_ATTRIBUTE_NAME);
        if (access == null) {
            return super.usesFieldAccess(mappedClass);
        }
        return FIELD_ACCESS.equals(access.getNodeValue().toUpperCase());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isMapped(Class<?> mappedClass) {
        return getMappedClassNode(mappedClass) != null;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isMapped(Member member) {
        String name = getName(member);
        Node classNode = getMappedClassNode(member.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        for (int i = 0; i < attributesNode.getChildNodes().getLength(); i++) {
            Node child = attributesNode.getChildNodes().item(i);
            if (!TRANSIENT_TAG_NAME.equals(child.getNodeName())) {
                XmlNodeList children = new XmlNodeList(child.getChildNodes());
                if (!children.subList(NAME_ATTRIBUTE_NAME, name).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isIdProperty(Member property) {
        String name = getName(property);
        Node entityNode = getEntityNode(property.getDeclaringClass());
        Node attributesNode = new XmlNodeList(entityNode.getChildNodes()).subList(ATTRIBUTES_TAG_NAME).get(0);
        XmlNodeList childNodes = new XmlNodeList(attributesNode.getChildNodes());
        List<Node> idNodes = childNodes.subList(ID_TAG_NAME);
        for (Node id: idNodes) {
            if (name.equals(id.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue())) {
                return true;
            }
        }
        List<Node> embeddedIdNodes = childNodes.subList(EMBEDDED_ID_TAG_NAME);
        for (Node embeddedId: embeddedIdNodes) {
            if (name.equals(embeddedId.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    protected CascadeType[] getCascadeTypes(Member property) {
        try {
            String query
                = MessageFormat.format(CASCADE_TYPE_XPATH, property.getDeclaringClass().getName(), getName(property));
            NodeList list = (NodeList)XPATH.evaluate(query, ormDocument, XPathConstants.NODESET);
            List<CascadeType> cascadeTypes = new ArrayList<CascadeType>(list.getLength());
            for (int i = 0; i < list.getLength(); i++) {
                cascadeTypes.add(CascadeType.valueOf(list.item(i).getNodeName().substring(8).toUpperCase()));
            }
            return cascadeTypes.toArray(new CascadeType[cascadeTypes.size()]);
        } catch (XPathExpressionException e) {
            LOG.error("Error while reading cascade style.", e);
            return new CascadeType[0];
        }
    }

    protected boolean isSingleValuedRelationshipProperty(Member property) {
        String name = getName(property);
        Node classNode = getMappedClassNode(property.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        for (int i = 0; i < attributesNode.getChildNodes().getLength(); i++) {
            Node child = attributesNode.getChildNodes().item(i);
            if (EMBEDDED_ID_TAG_NAME.equals(child.getNodeName())
                || MANY_TO_ONE_TAG_NAME.equals(child.getNodeName())
                || ONE_TO_ONE_TAG_NAME.equals(child.getNodeName())) {
                if (child.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isCollectionValuedRelationshipProperty(Member property) {
        String name = getName(property);
        Node classNode = getMappedClassNode(property.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        for (int i = 0; i < attributesNode.getChildNodes().getLength(); i++) {
            Node child = attributesNode.getChildNodes().item(i);
            if (ONE_TO_MANY_TAG_NAME.equals(child.getNodeName())
                || MANY_TO_MANY_TAG_NAME.equals(child.getNodeName())) {
                if (child.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void parse(PersistenceUnitInfo persistenceUnit, String mappingFilename) {
        try {
            for (Enumeration<URL> mappings = getResources(mappingFilename); mappings.hasMoreElements();) {
                parse(persistenceUnit, mappings.nextElement().openStream());
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private void parse(PersistenceUnitInfo persistenceUnit, InputStream stream) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            parse(persistenceUnit, builder.parse(stream));
        } catch (ParserConfigurationException e) {
            throw new PersistenceException(e);
        } catch (SAXException e) {
            throw new PersistenceException(e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        } finally {
            stream.close();
        }
    }

    private void parse(PersistenceUnitInfo persistenceUnit, Document mappingDocument) {
        entityNodes = new XmlNodeList(mappingDocument.getElementsByTagName(ENTITY_TAG_NAME));
        superclassNodes = new XmlNodeList(mappingDocument.getElementsByTagName(MAPPED_SUPERCLASS_TAG_NAME));
        embeddableNodes = new XmlNodeList(mappingDocument.getElementsByTagName(EMBEDDABLE_TAG_NAME));
        ormDocument = mappingDocument;
        parseNamedQueries();
        if (persistenceUnit.excludeUnlistedClasses()) {
            for (String className: persistenceUnit.getManagedClassNames()) {
                parse(getClass(className));
            }
            for (URL url: persistenceUnit.getJarFileUrls()) {
                parse(url);
            }
        } else {
            for (Node node: entityNodes) {
                parse(getClass(getClassName(node)));
            }
            for (Node node: superclassNodes) {
                parse(getClass(getClassName(node)));
            }
            for (Node node: embeddableNodes) {
                parse(getClass(getClassName(node)));
            }
        }
    }

    private void parseNamedQueries() {
        NodeList entries = null;
        try {
            entries = (NodeList)XPATH.evaluate(NAMED_QUERY_XPATH, ormDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOG.error("Error while reading named queries.", e);
            return;
        }
        for (int index = 0; index < entries.getLength(); index++) {
            Element namedQueryElement = (Element)entries.item(index);
            String name = namedQueryElement.getAttribute("name");
            NodeList queryList = namedQueryElement.getElementsByTagName("query");
            String query = ((Text)((Element)queryList.item(0)).getFirstChild()).getData();
            LOG.info("Adding query to query map. Name: '" + name + "'.");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Query: '" + query.trim() + "'.");
            }
            addNamedQuery(name, query);
        }
    }

    private String getClassName(Node classNode) {
        Node classAttribute = classNode.getAttributes().getNamedItem(OrmXmlParser.CLASS_ATTRIBUTE_NAME);
        return classAttribute.getNodeValue();
    }

    private Node getEntityNode(Class<?> entityClass) {
        List<Node> nodes = entityNodes.subList(CLASS_ATTRIBUTE_NAME, entityClass.getName());
        if (nodes.isEmpty()) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    private Node getMappedClassNode(Class<?> mappedClass) {
        Node entityClassNode = getEntityNode(mappedClass);
        if (entityClassNode != null) {
            return entityClassNode;
        }
        List<Node> nodes = superclassNodes.subList(CLASS_ATTRIBUTE_NAME, mappedClass.getName());
        if (!nodes.isEmpty()) {
            return nodes.get(0);
        }
        nodes = embeddableNodes.subList(CLASS_ATTRIBUTE_NAME, mappedClass.getName());
        return nodes.isEmpty()? null: nodes.get(0);
    }

    private Node getAttributesNode(Node classNode) {
        for (int i = 0; i < classNode.getChildNodes().getLength(); i++) {
            Node child = classNode.getChildNodes().item(i);
            if (ATTRIBUTES_TAG_NAME.equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }
}
