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

import java.util.List;

/**
 * @author Arne Limburg
 */
public class FieldAccessXmlTestBean {

    private int id;
    private String name;
    private Object parent;
    private List<FieldAccessXmlTestBean> children;

    public int getIdentifier() {
        return id;
    }

    public void setIdentifier(int identifier) {
        id = identifier;
    }

    public String getBeanName() {
        return name;
    }

    public void setBeanName(String beanName) {
        name = beanName;
    }

    public Object getParentBean() {
        return parent;
    }

    public void setParentBean(Object parentBean) {
        parent = parentBean;
    }

    public List<FieldAccessXmlTestBean> getChildBeans() {
        return children;
    }

    public void setChildren(List<FieldAccessXmlTestBean> childBeans) {
        children = childBeans;
    }
}
