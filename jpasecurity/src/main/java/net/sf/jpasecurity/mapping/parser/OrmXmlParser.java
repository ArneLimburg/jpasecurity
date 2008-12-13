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
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.xml.XmlNodeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Parser to parse orm.xml <strong>This class is not thread-safe</strong>
 *
 * @todo support <xml-mapping-metadata-complete/> tag
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class OrmXmlParser extends AbstractMappingParser {

    private static final Log LOG = LogFactory.getLog(OrmXmlParser.class);

    public static final String NAMED_QUERY_XPATH
        = "//named-query";
    public static final String CASCADE_TYPE_XPATH
        = "//entity[@class=''{0}'']//*[@name=''{1}'']/cascade/*";
    public static final String PACKAGE_XPATH
        = "//package";
    public static final String ENTITIES_XPATH
        = "//entity";
    public static final String MAPPED_SUPERCLASSES_XPATH
        = "//mapped-superclass";
    public static final String EMBEDDABLES_XPATH
        = "//embeddable";
    public static final String ENTITY_XPATH
        = "//entity[@class=''{0}'']";
    public static final String MAPPED_SUPERCLASS_XPATH
        = "//mapped-superclass[@class=''{0}'']";
    public static final String EMBEDDABLE_XPATH
        = "//embeddable[@class=''{0}'']";
    public static final String CLASS_XPATH
        = "//*[@class=''{0}'']";
    public static final String ID_CLASS_XPATH
        = "//*[@class=''{0}'']//id-class/@class";
    public static final String GLOBAL_ACCESS_TYPE_XPATH
        = "//persistence-unit-defaults/access";
    public static final String ACCESS_TYPE_XPATH
        = "//*[@class=''{0}'']/@access";
    public static final String ID_PROPERTY_XPATH
        = "//*[@class=''{0}'']//id[@name=''{1}'']";
    public static final String EMBEDDED_ID_PROPERTY_XPATH
        = "//*[@class=''{0}'']/attributes/embedded-id[@name=''{1}'']";
    public static final String TRANSIENT_PROPERTY_XPATH
        = "//*[@class=''{0}'']/attributes/transient[@name=''{1}'']";

    /**
     * The tag name for attributes
     */
    public static final String ATTRIBUTES_TAG_NAME = "attributes";

    /**
     * The tag name for embedded ids
     */
    public static final String EMBEDDED_ID_TAG_NAME = "embedded-id";

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
     * The tag prefix for cascade styles
     */
    public static final String CASCADE_TAG_PREFIX = "cascade-";

    /**
     * The attribute name for classes
     */
    public static final String CLASS_ATTRIBUTE_NAME = "class";

    /**
     * The attribute name for names
     */
    public static final String NAME_ATTRIBUTE_NAME = "name";

    /**
     * The field access type value
     */
    public static final String FIELD_ACCESS = "FIELD";

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();;

    private Document ormDocument;

    public void parsePersistenceUnit(PersistenceUnitInfo persistenceUnit) {
        parse(persistenceUnit, "META-INF/orm.xml");
        for (String mappingFilename: persistenceUnit.getMappingFileNames()) {
            parse(persistenceUnit, mappingFilename);
        }
    }

    protected Class<?> getIdClass(Class<?> entityClass, boolean useFieldAccess) {
        Node idClassNode = evaluateNode(ID_CLASS_XPATH, entityClass.getName());
        return idClassNode == null? null: getClass(idClassNode.getTextContent());
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isEmbeddable(Class<?> type) {
        return evaluateNode(EMBEDDABLE_XPATH, type.getName()) != null;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Node accessNode = getAccessTypeNode(mappedClass);
        if (accessNode == null) {
            return super.usesFieldAccess(mappedClass);
        }
        return FIELD_ACCESS.equals(accessNode.getTextContent().toUpperCase());
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
        if (evaluateNode(ID_PROPERTY_XPATH, property.getDeclaringClass(), name) != null) {
            return true;
        } else {
            return evaluateNode(EMBEDDED_ID_PROPERTY_XPATH, property.getDeclaringClass(), name) != null;
        }
    }

    protected CascadeType[] getCascadeTypes(Member property) {
        XmlNodeList list
            = evaluateNodes(CASCADE_TYPE_XPATH, property.getDeclaringClass().getName(), getName(property));
        List<CascadeType> cascadeTypes = new ArrayList<CascadeType>(list.size());
        for (Node node: list) {
            String cascadeType = node.getNodeName().substring(CASCADE_TAG_PREFIX.length());
            cascadeTypes.add(CascadeType.valueOf(cascadeType.toUpperCase()));
        }
        return cascadeTypes.toArray(new CascadeType[cascadeTypes.size()]);
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

    protected boolean isMappable(Member property) {
        if (evaluateNode(TRANSIENT_PROPERTY_XPATH, property.getDeclaringClass(), getName(property)) != null) {
            return false;
        } else {
            return super.isMappable(property);
        }
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
        ormDocument = mappingDocument;
        parseNamedQueries();
        String packageName = getPackageName();
        for (Node node: evaluateNodes(ENTITIES_XPATH)) {
            parse(node, packageName);
        }
        for (Node node: evaluateNodes(MAPPED_SUPERCLASSES_XPATH)) {
            parse(node, packageName);
        }
        for (Node node: evaluateNodes(EMBEDDABLES_XPATH)) {
            parse(node, packageName);
        }
    }

    private ClassMappingInformation parse(Node node, String defaultPackage) {
        String className = getClassName(node);
        if (defaultPackage != null) {
            try {
                return parse(getClass(defaultPackage + '.' + className));
            } catch (PersistenceException e) {
                if (e.getCause() instanceof ClassNotFoundException) {
                    try {
                        return parse(getClass(className));
                    } catch (PersistenceException c) {
                        if (c.getCause() instanceof ClassNotFoundException) {
                            throw className.indexOf('.') != -1? c: e;
                        }
                    }
                }
                throw e;
            }
        } else {
            return parse(getClass(className));
        }
    }

    private void parseNamedQueries() {
        XmlNodeList entries = evaluateNodes(NAMED_QUERY_XPATH);
        for (Node node: entries) {
            Element namedQueryElement = (Element)node;
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

    private String getPackageName() {
        Node packageNode = evaluateNode(PACKAGE_XPATH);
        return packageNode == null? null: packageNode.getTextContent();
    }

    private String getClassName(Node classNode) {
        Node classAttribute = classNode.getAttributes().getNamedItem(OrmXmlParser.CLASS_ATTRIBUTE_NAME);
        return classAttribute.getNodeValue();
    }

    private Node getMappedClassNode(Class<?> mappedClass) {
        return evaluateNode(CLASS_XPATH, mappedClass);
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

    private Node getAccessTypeNode(Class<?> mappedClass) {
        Node accessNode = evaluateNode(ACCESS_TYPE_XPATH, mappedClass);
        return accessNode != null? accessNode: evaluateNode(GLOBAL_ACCESS_TYPE_XPATH);
    }

    private Node evaluateNode(String query, Class<?> mappedClass, Object... parameters) {
        Object[] extendedParameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, extendedParameters, 1, parameters.length);
        String packageName = getPackageName();
        String className = mappedClass.getName();
        String prefix = packageName + '.';
        if (className.startsWith(prefix)) {
            extendedParameters[0] = className.substring(prefix.length());
            Node accessNode = evaluateNode(query, extendedParameters);
            if (accessNode != null) {
                return accessNode;
            }
        }
        extendedParameters[0] = className;
        return evaluateNode(query, extendedParameters);
    }

    private Node evaluateNode(String query, Object... parameters) {
        return (Node)evaluate(query, XPathConstants.NODE, parameters);
    }

    private XmlNodeList evaluateNodes(String query, Object... parameters) {
        return new XmlNodeList((NodeList)evaluate(query, XPathConstants.NODESET, parameters));
    }

    private Object evaluate(String query, QName resultType, Object... parameters) {
        try {
            query = MessageFormat.format(query, parameters);
            return (NodeList)XPATH.evaluate(query, ormDocument, resultType);
        } catch (XPathExpressionException e) {
            throw new PersistenceException(e);
        }
    }
}
