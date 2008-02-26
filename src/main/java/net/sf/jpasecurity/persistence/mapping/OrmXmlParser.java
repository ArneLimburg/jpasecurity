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

import net.sf.jpasecurity.xml.XmlNodeList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Parser to parse orm.xml
 * @todo support <xml-mapping-metadata-complete/> tag
 * @author Arne Limburg
 */
public class OrmXmlParser extends AbstractMappingParser {

    public static final String ENTITY_TAG_NAME = "entity";
    public static final String MAPPED_SUPERCLASS_TAG_NAME = "mapped-superclass";
    public static final String EMBEDDABLE_TAG_NAME = "embeddable";
    public static final String ATTRIBUTES_TAG_NAME = "attributes";
    public static final String EMBEDDED_ID_TAG_NAME = "embedded-id";
    public static final String ID_TAG_NAME = "id";
    public static final String MANY_TO_ONE_TAG_NAME = "many-to-one";
    public static final String ONE_TO_ONE_TAG_NAME = "one-to-one";
    public static final String ONE_TO_MANY_TAG_NAME = "one-to-many";
    public static final String MANY_TO_MANY_TAG_NAME = "many-to-many";
    public static final String TRANSIENT_TAG_NAME = "transient";
    public static final String CLASS_ATTRIBUTE_NAME = "class";
    public static final String NAME_ATTRIBUTE_NAME = "name";
    
    private XmlNodeList entityNodes;
    private XmlNodeList superclassNodes;
    private XmlNodeList embeddableNodes;
    
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
    
    protected boolean isEmbeddable(Class<?> type) {
        return embeddableNodes.containsAttribute(CLASS_ATTRIBUTE_NAME, type.getName());
    }

    protected boolean isMapped(Class<?> mappedClass) {
        return getMappedClassNode(mappedClass) != null;
    }

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
    
    private Node getMappedClassNode(Class<?> mappedClass) {
        List<Node> nodes = entityNodes.subList(CLASS_ATTRIBUTE_NAME, mappedClass.getName());
        if (!nodes.isEmpty()){
            return nodes.get(0);
        }
        nodes = superclassNodes.subList(CLASS_ATTRIBUTE_NAME, mappedClass.getName());
        if (!nodes.isEmpty()){
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
