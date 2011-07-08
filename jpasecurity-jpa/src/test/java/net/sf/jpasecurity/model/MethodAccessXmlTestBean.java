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
package net.sf.jpasecurity.model;

import java.util.List;

/**
 * @author Arne Limburg
 */
public class MethodAccessXmlTestBean {

    private int identifier;
    private String beanName;
    private MethodAccessXmlTestBean parentBean;
    private List<MethodAccessXmlTestBean> childBeans;

    public int getId() {
        return identifier;
    }

    public void setId(int id) {
        identifier = id;
    }

    public String getName() {
        return beanName;
    }

    public void setName(String name) {
        beanName = name;
    }

    public MethodAccessXmlTestBean getParent() {
        return parentBean;
    }

    public void setParent(MethodAccessXmlTestBean parent) {
        parentBean = parent;
    }

    public List<MethodAccessXmlTestBean> getChildren() {
        return childBeans;
    }

    public void setChildren(List<MethodAccessXmlTestBean> children) {
        childBeans = children;
    }
}
