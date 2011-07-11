/*
 * Copyright 2011 Arne Limburg
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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Arne Limburg
 */
public class ComposedNodeList implements NodeList {

    private List<NodeList> nodeLists = new ArrayList<NodeList>();

    public void add(NodeList nodeList) {
        nodeLists.add(nodeList);
    }

    public Node item(int index) {
        int i = 0;
        for (NodeList nodeList: nodeLists) {
            if (index < i + nodeList.getLength()) {
                return nodeList.item(index - i);
            }
            i += nodeList.getLength();
        }
        return null;
    }

    public int getLength() {
        int length = 0;
        for (NodeList nodeList: nodeLists) {
            length += nodeList.getLength();
        }
        return length;
    }
}
