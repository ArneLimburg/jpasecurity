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

package net.sf.jpasecurity.xml;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Arne Limburg
 */
public class XmlNodeList extends AbstractList<Node> {

    private NodeList nodes;

    public XmlNodeList(NodeList nodeList) {
        nodes = nodeList;
    }

    public List<Node> subList(String attributeName, String attributeValue) {
        List<Node> nodes = new ArrayList<Node>();
        for (Node node: this) {
            if (node.getAttributes().getNamedItem(attributeName).getTextContent().equals(attributeValue)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    public boolean containsAttribute(String attributeName, String attributeValue) {
        return subList(attributeName, attributeValue).size() > 0;
    }

    public Node get(int index) {
        return nodes.item(index);
    }

    public int size() {
        return nodes.getLength();
    }
}
