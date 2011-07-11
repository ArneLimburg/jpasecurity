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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Arne Limburg
 */
public class SingletonNodeList implements NodeList {

    private Node node;

    public SingletonNodeList(Node node) {
        this.node = node;
    }

    public Node item(int index) {
        if (index == 0) {
            return node;
        }
        return null;
    }

    public int getLength() {
        return node != null? 1: 0;
    }
}
