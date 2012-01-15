/*
 * Copyright 2008 - 2011 Arne Limburg
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
package net.sf.jpasecurity.persistence.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javassist.bytecode.CodeAttribute.RuntimeCopyException;

import javax.persistence.FetchType;
import javax.persistence.PersistenceException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.DefaultClassMappingInformation;
import net.sf.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory;
import net.sf.jpasecurity.mapping.EntityLifecycleMethods;
import net.sf.jpasecurity.mapping.EntityListenerWrapper;
import net.sf.jpasecurity.mapping.PropertyAccessStrategyFactory;
import net.sf.jpasecurity.xml.EmptyNodeList;

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
 * Parser to parse orm.xml <strong>This class is not thread-safe</strong>
 *
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class OrmXmlParser extends JpaAnnotationParser {

    private static final Log LOG = LogFactory.getLog(OrmXmlParser.class);

    public static final String NAMED_QUERY_XPATH
        = "//named-query";
    public static final String DEFAULT_ENTITY_LISTENER_XPATH
        = "//persistence-unit-defaults/entity-listeners/entity-listener";
    public static final String XML_MAPPING_METADATA_COMPLETE_XPATH
        = "//persistence-unit-defaults/xml-mapping-metadata-complete";
    public static final String METADATA_COMPLETE_XPATH
        = "//*[@class=''{0}'']/@metadata-complete";
    public static final String EXCLUDE_DEFAULT_LISTENERS_XPATH
        = "//*[@class=''{0}'']/@exclude-default-listeners";
    public static final String EXCLUDE_SUPERCLASS_LISTENERS_XPATH
        = "//*[@class=''{0}'']/@exclude-superclass-listeners";
    public static final String PRE_PERSIST_XPATH
        = "//*[@class=''{0}'']/pre-persist";
    public static final String POST_PERSIST_XPATH
        = "//*[@class=''{0}'']/post-persist";
    public static final String PRE_REMOVE_XPATH
        = "//*[@class=''{0}'']/pre-remove";
    public static final String POST_REMOVE_XPATH
        = "//*[@class=''{0}'']/post-remove";
    public static final String PRE_UPDATE_XPATH
        = "//*[@class=''{0}'']/pre-update";
    public static final String POST_UPDATE_XPATH
        = "//*[@class=''{0}'']/post-update";
    public static final String POST_LOAD_XPATH
        = "//*[@class=''{0}'']/post-load";
    public static final String ENTITY_LISTENERS_XPATH
        = "//*[@class=''{0}'']/entity-listeners/entity-listener";
    public static final String FETCH_TYPE_XPATH
        = "//*[@class=''{0}'']//*[@name=''{1}'']/@fetch";
    public static final String CASCADE_TYPE_XPATH
        = "//*[@class=''{0}'']//*[@name=''{1}'']/cascade";
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
    public static final String VERSION_PROPERTY_XPATH
        = "//*[@class=''{0}'']//version[@name=''{1}'']";
    public static final String GENERATED_VALUE_PROPERTY_XPATH
        = "//*[@class=''{0}'']//id[@name=''{1}'']/generated-value";
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

    private Collection<Document> mappingDocuments = new ArrayList<Document>();

    public OrmXmlParser(SecurityUnit securityUnit, ExceptionFactory exceptionFactory) {
        this(securityUnit, new DefaultPropertyAccessStrategyFactory(), exceptionFactory);
    }

    public OrmXmlParser(SecurityUnit securityUnit,
                        PropertyAccessStrategyFactory propertyAccessStrategyFactory,
                        ExceptionFactory exceptionFactory) {
        super(securityUnit, propertyAccessStrategyFactory, exceptionFactory);
    }

    @Override
    public void parseSecurityUnit(SecurityUnit securityUnit) {
        parse(securityUnit, "META-INF/orm.xml");
        for (String mappingFilename: securityUnit.getMappingFileNames()) {
            parse(securityUnit, mappingFilename);
        }
        super.parseSecurityUnit(securityUnit);
        for (Document mappingDocument: mappingDocuments) {
            parse(securityUnit, mappingDocument);
        }
    }

    @Override
    protected String getEntityName(Class<?> entityClass) {
        Node entityNode = evaluateNode(ENTITY_XPATH, entityClass);
        Node entityName = entityNode == null? null: entityNode.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME);
        if (entityName != null) {
            return entityName.getNodeValue();
        }
        if (!isMetadataComplete(entityClass)) {
            return super.getEntityName(entityClass);
        }
        return entityClass.getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess) {
        Node idClassNode = evaluateNode(ID_CLASS_XPATH, entityClass);
        if (idClassNode != null) {
            return getClass(idClassNode.getTextContent());
        }
        if (!isMetadataComplete(entityClass)) {
            return super.getIdClass(entityClass, usesFieldAccess);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEmbeddable(Class<?> type) {
        Document mappingDocument = getMappingDocument(type);
        if (evaluateNode(mappingDocument, EMBEDDABLE_XPATH, type) != null) {
            return true;
        }
        if (!isMetadataComplete(mappingDocument)) {
            return super.isEmbeddable(type);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Node accessNode = getAccessTypeNode(mappedClass);
        if (accessNode != null) {
            return FIELD_ACCESS.equals(accessNode.getTextContent().toUpperCase());
        }
        if (!isMetadataComplete(mappedClass)) {
            return super.usesFieldAccess(mappedClass);
        }
        return false;
    }

    @Override
    protected boolean excludeDefaultEntityListeners(Class<?> entityClass) {
        if (evaluateNode(EXCLUDE_DEFAULT_LISTENERS_XPATH, entityClass) != null) {
            return true;
        }
        if (!isMetadataComplete(entityClass)) {
            return super.excludeDefaultEntityListeners(entityClass);
        }
        return false;
    }

    @Override
    protected boolean excludeSuperclassEntityListeners(Class<?> entityClass) {
        if (evaluateNode(EXCLUDE_SUPERCLASS_LISTENERS_XPATH, entityClass) != null) {
            return true;
        }
        if (!isMetadataComplete(entityClass)) {
            return super.excludeSuperclassEntityListeners(entityClass);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isMapped(Class<?> mappedClass) {
        if (getMappedClassNode(mappedClass) != null) {
            return true;
        }
        if (!isMetadataComplete(mappedClass)) {
            return super.isMapped(mappedClass);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isMapped(Member member) {
        String name = getName(member);
        Node classNode = getMappedClassNode(member.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        if (attributesNode != null) {
            for (int index = 0; index < attributesNode.getChildNodes().getLength(); index++) {
                Node child = attributesNode.getChildNodes().item(index);
                if (!TRANSIENT_TAG_NAME.equals(child.getNodeName())) {
                    NodeList children = child.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        NamedNodeMap attributes = children.item(i).getAttributes();
                        if (attributes != null) {
                            Node namedItem = attributes.getNamedItem(NAME_ATTRIBUTE_NAME);
                            if (namedItem != null && namedItem.getTextContent().equals(name)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (!isMetadataComplete(member.getDeclaringClass())) {
            return super.isMapped(member);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIdProperty(Member property) {
        String name = getName(property);
        if (evaluateNode(ID_PROPERTY_XPATH, property.getDeclaringClass(), name) != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Id metadata found for "
                          + property.getDeclaringClass().getSimpleName() + "." + getName(property));
            }
            return true;
        }
        if (evaluateNode(EMBEDDED_ID_PROPERTY_XPATH, property.getDeclaringClass(), name) != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Embedded-id metadata found for "
                          + property.getDeclaringClass().getSimpleName() + "." + getName(property));
            }
            return true;
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("No id metadata found for "
                          + property.getDeclaringClass().getSimpleName() + "." + getName(property)
                          + ", looking for annotations.");
            }
            return super.isIdProperty(property);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("No id metadata found for "
                      + property.getDeclaringClass().getSimpleName() + "." + getName(property)
                      + " and metadata is complete.");
        }
        return false;
    }

    protected boolean isMetadataComplete(Document mappingDocument) {
        return evaluateNode(mappingDocument, XML_MAPPING_METADATA_COMPLETE_XPATH) != null;
    }

    @Override
    protected boolean isMetadataComplete(Class<?> entityClass) {
        if (isMetadataComplete(getMappingDocument(entityClass))) {
            return true;
        }
        Node metadataCompleteNode = evaluateNode(METADATA_COMPLETE_XPATH, entityClass);
        if (metadataCompleteNode == null) {
            return false;
        }
        return Boolean.valueOf(metadataCompleteNode.getTextContent().trim());
    }

    @Override
    protected boolean isVersionProperty(Member property) {
        String name = getName(property);
        if (evaluateNode(VERSION_PROPERTY_XPATH, property.getDeclaringClass(), name) != null) {
            return true;
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            return super.isVersionProperty(property);
        }
        return false;
    }

    protected boolean isGeneratedValue(Member property) {
        String name = getName(property);
        if (evaluateNode(GENERATED_VALUE_PROPERTY_XPATH, property.getDeclaringClass(), name) != null) {
            return true;
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            return super.isGeneratedValue(property);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFetchTypePresent(Member property) {
        if (getXmlFetchType(property) != null) {
            return true;
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            return super.isFetchTypePresent(property);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected net.sf.jpasecurity.FetchType getFetchType(Member property) {
        FetchType fetchType = getXmlFetchType(property);
        if (fetchType != null) {
            return net.sf.jpasecurity.FetchType.valueOf(fetchType.name());
        }
        return super.getFetchType(property);
    }

    protected FetchType getXmlFetchType(Member property) {
        Node node = evaluateNode(FETCH_TYPE_XPATH, property.getDeclaringClass(), getName(property));
        if (node == null) {
            return null;
        }
        return FetchType.valueOf(node.getTextContent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CascadeType[] getCascadeTypes(Member property) {
        Class<?> declaringClass = property.getDeclaringClass();
        Document mappingDocument = getMappingDocument(declaringClass);
        NodeList list = evaluateNodes(mappingDocument, CASCADE_TYPE_XPATH, declaringClass, getName(property));
        if (list == null && !isMetadataComplete(property.getDeclaringClass())) {
            return super.getCascadeTypes(property);
        }
        List<CascadeType> cascadeTypes = new ArrayList<CascadeType>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String cascadeType = list.item(i).getNodeName().substring(CASCADE_TAG_PREFIX.length());
                cascadeTypes.add(CascadeType.valueOf(cascadeType.toUpperCase()));
            }
        }
        return cascadeTypes.toArray(new CascadeType[cascadeTypes.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isSingleValuedRelationshipProperty(Member property) {
        String name = getName(property);
        Node classNode = getMappedClassNode(property.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        if (attributesNode != null) {
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
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            return super.isSingleValuedRelationshipProperty(property);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCollectionValuedRelationshipProperty(Member property) {
        String name = getName(property);
        Node classNode = getMappedClassNode(property.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        if (attributesNode != null) {
            for (int i = 0; i < attributesNode.getChildNodes().getLength(); i++) {
                Node child = attributesNode.getChildNodes().item(i);
                if (ONE_TO_MANY_TAG_NAME.equals(child.getNodeName())
                                || MANY_TO_MANY_TAG_NAME.equals(child.getNodeName())) {
                    if (child.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getNodeValue().equals(name)) {
                        return true;
                    }
                }
            }
        }
        if (!isMetadataComplete(property.getDeclaringClass())) {
            return super.isCollectionValuedRelationshipProperty(property);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isMappable(Member property) {
        if (evaluateNode(TRANSIENT_PROPERTY_XPATH, property.getDeclaringClass(), getName(property)) != null) {
            return false;
        } else {
            return super.isMappable(property);
        }
    }

    private void parse(SecurityUnit securityUnit, String mappingFilename) {
        try {
            for (Enumeration<URL> mappings = getResources(mappingFilename); mappings.hasMoreElements();) {
                mappingDocuments.add(parse(securityUnit, mappings.nextElement().openStream()));
            }
        } catch (IOException e) {
            throw exceptionFactory.createRuntimeException(e);
        }
    }

    private Document parse(SecurityUnit securityUnit, InputStream stream) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(stream);
        } catch (ParserConfigurationException e) {
            throw exceptionFactory.createRuntimeException(e);
        } catch (SAXException e) {
            throw exceptionFactory.createRuntimeException(e);
        } catch (IOException e) {
            throw exceptionFactory.createRuntimeException(e);
        } finally {
            stream.close();
        }
    }

    private void parse(SecurityUnit securityUnit, Document mappingDocument) {
        parseNamedQueries(mappingDocument);
        parseDefaultEntityListeners(mappingDocument);
        String packageName = getPackageName(mappingDocument);
        NodeList list = evaluateNodes(mappingDocument, ENTITIES_XPATH);
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                parse(list.item(i), packageName);
            }
        }
        list = evaluateNodes(mappingDocument, MAPPED_SUPERCLASSES_XPATH);
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                parse(list.item(i), packageName);
            }
        }
        list = evaluateNodes(mappingDocument, EMBEDDABLES_XPATH);
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                parse(list.item(i), packageName);
            }
        }
    }

    private ClassMappingInformation parse(Node node, String defaultPackage) {
        String className = getClassName(node);
        if (defaultPackage != null) {
            try {
                return parse(getClass(defaultPackage + '.' + className), false, true);
            } catch (RuntimeException e) {
                if (e.getCause() instanceof ClassNotFoundException) {
                    try {
                        return parse(getClass(className), false, true);
                    } catch (RuntimeCopyException c) {
                        if (c.getCause() instanceof ClassNotFoundException) {
                            throw className.indexOf('.') != -1? c: e;
                        }
                    }
                }
                throw e;
            }
        } else {
            return parse(getClass(className), false, true);
        }
    }

    private void parseNamedQueries(Document mappingDocument) {
        NodeList entries = evaluateNodes(mappingDocument, NAMED_QUERY_XPATH);
        if (entries == null) {
            return;
        }
        for (int i = 0; i < entries.getLength(); i++) {
            Element namedQueryElement = (Element)entries.item(i);
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

    @Override
    protected void parseEntityLifecycleMethods(DefaultClassMappingInformation classMapping) {
        EntityLifecycleMethods entityLifecycleMethods = null;
        if (isMetadataComplete(classMapping.getEntityType())) {
            entityLifecycleMethods = new EntityLifecycleMethods();
        } else {
            entityLifecycleMethods = super.parseEntityLifecycleMethods(classMapping.getEntityType());
        }
        Node prePersistNode = evaluateNode(PRE_PERSIST_XPATH, classMapping.getEntityType());
        Node postPersistNode = evaluateNode(POST_PERSIST_XPATH, classMapping.getEntityType());
        Node preRemoveNode = evaluateNode(PRE_REMOVE_XPATH, classMapping.getEntityType());
        Node postRemoveNode = evaluateNode(POST_REMOVE_XPATH, classMapping.getEntityType());
        Node preUpdateNode = evaluateNode(PRE_UPDATE_XPATH, classMapping.getEntityType());
        Node postUpdateNode = evaluateNode(POST_UPDATE_XPATH, classMapping.getEntityType());
        Node postLoadNode = evaluateNode(POST_LOAD_XPATH, classMapping.getEntityType());
        if (prePersistNode != null) {
            entityLifecycleMethods.setPrePersistMethod(getMethod(classMapping.getEntityType(), prePersistNode, 0));
        }
        if (postPersistNode != null) {
            entityLifecycleMethods.setPostPersistMethod(getMethod(classMapping.getEntityType(), postPersistNode, 0));
        }
        if (preRemoveNode != null) {
            entityLifecycleMethods.setPreRemoveMethod(getMethod(classMapping.getEntityType(), preRemoveNode, 0));
        }
        if (postRemoveNode != null) {
            entityLifecycleMethods.setPostRemoveMethod(getMethod(classMapping.getEntityType(), postRemoveNode, 0));
        }
        if (preUpdateNode != null) {
            entityLifecycleMethods.setPreUpdateMethod(getMethod(classMapping.getEntityType(), preUpdateNode, 0));
        }
        if (postUpdateNode != null) {
            entityLifecycleMethods.setPostUpdateMethod(getMethod(classMapping.getEntityType(), postUpdateNode, 0));
        }
        if (postLoadNode != null) {
            entityLifecycleMethods.setPostLoadMethod(getMethod(classMapping.getEntityType(), postLoadNode, 0));
        }
        setEntityLifecycleMethods(classMapping, entityLifecycleMethods);
    }

    @Override
    protected void parseEntityListeners(DefaultClassMappingInformation classMapping) {
        if (!isMetadataComplete(classMapping.getEntityType())) {
            super.parseEntityListeners(classMapping);
        }
        Element classNode = (Element)evaluateNode(CLASS_XPATH, classMapping.getEntityType());
        if (classNode == null) {
            return;
        }
        NodeList entityListeners = classNode.getElementsByTagName("entity-listener");
        for (int i = 0; i < entityListeners.getLength(); i++) {
            Class<?> type = getEntityListenerType(entityListeners.item(i));
            EntityLifecycleMethods entityLifecycleMethods = super.parseEntityLifecycleMethods(type);
            EntityListenerWrapper entityListener
                = parseEntityListener(entityListeners.item(i), type, entityLifecycleMethods);
            addEntityListener(classMapping, type, entityListener);
        }
    }

    private void parseDefaultEntityListeners(Document mappingDocument) {
        NodeList entityListeners = evaluateNodes(mappingDocument, DEFAULT_ENTITY_LISTENER_XPATH);
        for (int i = 0; i < entityListeners.getLength(); i++) {
            Class<?> type = getEntityListenerType(entityListeners.item(i));
            EntityLifecycleMethods entityLifecycleMethods = new EntityLifecycleMethods();
            addDefaultEntityListener(parseEntityListener(entityListeners.item(i), type, entityLifecycleMethods));
        }
    }

    private Class<?> getEntityListenerType(Node node) {
        Element entityListenerElement = (Element)node;
        String listenerClassName = entityListenerElement.getAttribute("class");
        return getClass(listenerClassName);
    }

    private EntityListenerWrapper parseEntityListener(Node node,
                                                      Class<?> listenerClass,
                                                      EntityLifecycleMethods entityLifecycleMethods) {
        Element entityListenerElement = (Element)node;
        try {
            Object listener = listenerClass.newInstance();
            NodeList prePersistNodes = entityListenerElement.getElementsByTagName("pre-persist");
            NodeList postPersistNodes = entityListenerElement.getElementsByTagName("post-persist");
            NodeList preRemoveNodes = entityListenerElement.getElementsByTagName("pre-remove");
            NodeList postRemoveNodes = entityListenerElement.getElementsByTagName("post-remove");
            NodeList preUpdateNodes = entityListenerElement.getElementsByTagName("pre-update");
            NodeList postUpdateNodes = entityListenerElement.getElementsByTagName("post-update");
            NodeList postLoadNodes = entityListenerElement.getElementsByTagName("post-load");
            Method prePersistMethod = getMethod(listenerClass, prePersistNodes, 1);
            Method postPersistMethod = getMethod(listenerClass, postPersistNodes, 1);
            Method preRemoveMethod = getMethod(listenerClass, preRemoveNodes, 1);
            Method postRemoveMethod = getMethod(listenerClass, postRemoveNodes, 1);
            Method preUpdateMethod = getMethod(listenerClass, preUpdateNodes, 1);
            Method postUpdateMethod = getMethod(listenerClass, postUpdateNodes, 1);
            Method postLoadMethod = getMethod(listenerClass, postLoadNodes, 1);
            if (prePersistMethod != null) {
                entityLifecycleMethods.setPrePersistMethod(prePersistMethod);
            }
            if (postPersistMethod != null) {
                entityLifecycleMethods.setPostPersistMethod(postPersistMethod);
            }
            if (preRemoveMethod != null) {
                entityLifecycleMethods.setPreRemoveMethod(preRemoveMethod);
            }
            if (postRemoveMethod != null) {
                entityLifecycleMethods.setPostRemoveMethod(postRemoveMethod);
            }
            if (preUpdateMethod != null) {
                entityLifecycleMethods.setPreUpdateMethod(preUpdateMethod);
            }
            if (postUpdateMethod != null) {
                entityLifecycleMethods.setPostUpdateMethod(postUpdateMethod);
            }
            if (postLoadMethod != null) {
                entityLifecycleMethods.setPostLoadMethod(postLoadMethod);
            }
            return new EntityListenerWrapper(listener, entityLifecycleMethods, exceptionFactory);
        } catch (InstantiationException e) {
            throw new PersistenceException("could not instantiate default entity-listener of type " + listenerClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException("could not instantiate default entity-listener of type " + listenerClass.getName(), e);
        }
    }

    private Method getMethod(Class<?> type, NodeList nodes, int parameterCount) {
        if (nodes.getLength() == 0) {
            return null;
        }
        if (nodes.getLength() > 1) {
            throw exceptionFactory.createMappingException("Only one method may be specified per lifecycle event");
        }
        return getMethod(type, nodes.item(0), parameterCount);
    }

    private Method getMethod(Class<?> type, Node node, int parameterCount) {
        if (node == null) {
            return null;
        }
        String methodName = node.getAttributes().getNamedItem("method-name").getTextContent().trim();
        while (type != null) {
            for (Method method: type.getDeclaredMethods()) {
                if (methodName.equals(method.getName()) && method.getParameterTypes().length == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private String getPackageName(Document mappingDocument) {
        Node packageNode = evaluateNode(mappingDocument, PACKAGE_XPATH);
        return packageNode == null? null: packageNode.getTextContent().trim();
    }

    private String getClassName(Node classNode) {
        Node classAttribute = classNode.getAttributes().getNamedItem(OrmXmlParser.CLASS_ATTRIBUTE_NAME);
        return classAttribute.getNodeValue();
    }

    private Node getMappedClassNode(Class<?> mappedClass) {
        return evaluateNode(CLASS_XPATH, mappedClass);
    }

    private Node getAttributesNode(Node classNode) {
        if (classNode == null) {
            return null;
        }
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
        return accessNode != null? accessNode: evaluateNode(getMappingDocument(mappedClass), GLOBAL_ACCESS_TYPE_XPATH);
    }

    private Document getMappingDocument(Class<?> mappedClass) {
        Document foundDocument = null;
        for (Document mappingDocument: mappingDocuments) {
            if (evaluateNode(mappingDocument, ENTITY_XPATH, mappedClass) != null
                || evaluateNode(mappingDocument, EMBEDDABLE_XPATH, mappedClass) != null
                || evaluateNode(mappingDocument, MAPPED_SUPERCLASS_XPATH, mappedClass) != null) {
//                checkDuplicateDefinition(mappedClass, foundDocument, mappingDocument);
                foundDocument = mappingDocument;
            }
        }
        return foundDocument;
    }

    private void checkDuplicateDefinition(Class<?> mappedClass, Document document1, Document document2) {
        if (document1 != null && document2 != null) {
            String message = "class " + mappedClass.getName() + " is definied in more than one xml-files";
            throw exceptionFactory.createRuntimeException(message);
        }
    }

    private Node evaluateNode(String query, Class<?> mappedClass, Object... parameters) {
        return evaluateNode(getMappingDocument(mappedClass), query, mappedClass, parameters);
    }

    private Node evaluateNode(Document document, String query, Class<?> mappedClass, Object... parameters) {
        Object[] extendedParameters = extendParameters(document, mappedClass, parameters);
        if (!mappedClass.getName().equals(extendedParameters[0])) {
            Node node = evaluateNode(document, query, extendedParameters);
            if (node != null) {
                return node;
            }
        }
        extendedParameters[0] = mappedClass.getName();
        return evaluateNode(document, query, extendedParameters);
    }

    private Node evaluateNode(Document document, String query, Object... parameters) {
        return (Node)evaluate(document, query, XPathConstants.NODE, parameters);
    }

    private NodeList evaluateNodes(Document document, String query, Class<?> mappedClass, Object... parameters) {
        Object[] extendedParameters = extendParameters(document, mappedClass, parameters);
        if (!mappedClass.getName().equals(extendedParameters[0])) {
            NodeList node = (NodeList)evaluateNode(getMappingDocument(mappedClass), query, extendedParameters);
            if (node != null) {
                return node;
            }
        }
        extendedParameters[0] = mappedClass.getName();
        return (NodeList)evaluateNode(getMappingDocument(mappedClass), query, extendedParameters);
    }

    private NodeList evaluateNodes(Document document, String query, Object... parameters) {
        Object nodeList = evaluate(document, query, XPathConstants.NODESET, parameters);
        return nodeList == null? new EmptyNodeList(): (NodeList)nodeList;
    }

    private Object[] extendParameters(Document document, Class<?> mappedClass, Object... parameters) {
        Object[] extendedParameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, extendedParameters, 1, parameters.length);
        String packageName = getPackageName(document);
        String className = mappedClass.getName();
        String prefix = packageName + '.';
        if (className.startsWith(prefix)) {
            extendedParameters[0] = className.substring(prefix.length());
        } else {
            extendedParameters[0] = className;
        }
        return extendedParameters;
    }

    private Object evaluate(Document mappingDocument, String query, QName resultType, Object... parameters) {
        if (mappingDocument == null) {
            return null;
        }
        query = MessageFormat.format(query, parameters);
        try {
            NodeList nodeList;
            Object result = XPATH.evaluate(query, mappingDocument, resultType);
            if (resultType.equals(XPathConstants.NODESET)) {
                nodeList = (NodeList)result;
            } else if (result != null) {
                return result;
            } else {
                nodeList = null;
            }
            return nodeList;
        } catch (XPathExpressionException e) {
            throw exceptionFactory.createRuntimeException(e);
        }
    }
}
