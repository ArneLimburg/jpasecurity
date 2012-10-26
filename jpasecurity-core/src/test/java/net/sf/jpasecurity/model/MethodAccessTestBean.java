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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arne Limburg
 */
public class MethodAccessTestBean {

    private int identifier;
    private String beanName;
    private boolean setNameCalled;
    private MethodAccessTestBean parentBean;
    private List<MethodAccessTestBean> childBeans = new ArrayList<MethodAccessTestBean>();
    private Map<MethodAccessTestBean, MethodAccessTestBean> map
        = new HashMap<MethodAccessTestBean, MethodAccessTestBean>();

    public MethodAccessTestBean() {
    }

    public MethodAccessTestBean(String name) {
        beanName = name;
    }

    public MethodAccessTestBean(int id, String name) {
        identifier = id;
        beanName = name;
    }

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
        setNameCalled = true;
        beanName = name;
    }

    public boolean wasSetNameCalled() {
        return setNameCalled;
    }

    public MethodAccessTestBean getParent() {
        return parentBean;
    }

    public void setParent(MethodAccessTestBean parent) {
        parentBean = parent;
    }

    public List<MethodAccessTestBean> getChildren() {
        return childBeans;
    }

    public void setChildren(List<MethodAccessTestBean> children) {
        childBeans = children;
    }

    public Map<MethodAccessTestBean, MethodAccessTestBean> getRelated() {
        return map;
    }

    public void setRelated(Map<MethodAccessTestBean, MethodAccessTestBean> related) {
        map = related;
    }
}
