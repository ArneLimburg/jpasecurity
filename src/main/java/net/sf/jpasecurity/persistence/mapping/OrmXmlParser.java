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
package net.sf.jpasecurity.persistence.mapping;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.xml.XmlNodeList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Parser to parse orm.xml
 * @todo support <xml-mapping-metadata-complete/> tag
 * @author Arne Limburg
 */
public class OrmXmlParser extends AbstractMappingParser {

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

    private XmlNodeList entityNodes;
    private XmlNodeList superclassNodes;
    private XmlNodeList embeddableNodes;

    /**
     * Creates a parser to parse a orm.xml file.
     */
    public OrmXmlParser(Map<Class<?>, ClassMappingInformation> classMappings, Document mappingDocument) {
        super(classMappings);
        entityNodes = new XmlNodeList(mappingDocument.getElementsByTagName(ENTITY_TAG_NAME));
        superclassNodes = new XmlNodeList(mappingDocument.getElementsByTagName(MAPPED_SUPERCLASS_TAG_NAME));
        embeddableNodes = new XmlNodeList(mappingDocument.getElementsByTagName(EMBEDDABLE_TAG_NAME));
    }

    public List<Node> getEntityNodes() {
        return entityNodes;
    }

    public List<Node> getSuperclassNodes() {
        return superclassNodes;
    }

    public List<Node> getEmbeddableNodes() {
        return embeddableNodes;
    }

    protected Class<?> getIdClass(Class<?> entityClass, boolean useFieldAccess) {
        Node entityNode = getEntityNode(entityClass);
        XmlNodeList childNodes = new XmlNodeList(entityNode.getChildNodes());
        List<Node> idClassNodes = childNodes.subList(ID_CLASS_TAG_NAME, "");
        if (idClassNodes.size() > 0) {
            try {
                return Class.forName(idClassNodes.get(0).getAttributes().getNamedItem(CLASS_ATTRIBUTE_NAME).getTextContent());
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
        XmlNodeList childNodes = new XmlNodeList(entityNode.getChildNodes());
        List<Node> idNodes = childNodes.subList(ID_TAG_NAME, "");
        for (Node id: idNodes) {
            if (name.equals(id.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getTextContent())) {
                return true;
            }
        }
        List<Node> embeddedIdNodes = childNodes.subList(EMBEDDED_ID_TAG_NAME, "");
        for (Node embeddedId: embeddedIdNodes) {
            if (name.equals(embeddedId.getAttributes().getNamedItem(NAME_ATTRIBUTE_NAME).getTextContent())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSingleValuedRelationshipProperty(Member property) {
        String name = getName(property);
        Node classNode = getMappedClassNode(property.getDeclaringClass());
        Node attributesNode = getAttributesNode(classNode);
        for (int i = 0; i < attributesNode.getChildNodes().getLength(); i++) {
            Node child = attributesNode.getChildNodes().item(i);
            if (EMBEDDED_ID_TAG_NAME.equals(child.getNodeName())
                || ID_TAG_NAME.equals(child.getNodeName())
                || MANY_TO_ONE_TAG_NAME.equals(child.getNodeName())
                || ONE_TO_ONE_TAG_NAME.equals(child.getNodeName())) {
                XmlNodeList children = new XmlNodeList(child.getChildNodes());
                if (!children.subList(NAME_ATTRIBUTE_NAME, name).isEmpty()) {
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
                XmlNodeList children = new XmlNodeList(child.getChildNodes());
                if (!children.subList(NAME_ATTRIBUTE_NAME, name).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
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
