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
package org.jpasecurity.model;

import java.util.ArrayList;
import java.util.List;

import org.jpasecurity.collection.SecureCollections;

/**
 * @author Stefan Hildebrandt
 */
public class ParentTestBean implements EmptyInterface {

    private int id;
    private String name;
    private int version;
    private List<ChildTestBean> children = new ArrayList<ChildTestBean>();

    public ParentTestBean() {
    }

    public ParentTestBean(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int identifier) {
        id = identifier;
    }

    public List<ChildTestBean> getChildren() {
        return SecureCollections.secureList(children);
    }

    public void setChildren(List<ChildTestBean> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
