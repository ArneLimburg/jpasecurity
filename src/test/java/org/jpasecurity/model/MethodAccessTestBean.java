/*
 * Copyright 2008 - 2016 Arne Limburg
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * @author Arne Limburg
 */
@NamedQueries({
    @NamedQuery(name = "MethodAccessTestBean.findById",
                query = "SELECT m FROM MethodAccessTestBean m WHERE m.id = :id"),
    @NamedQuery(name = "MethodAccessTestBean.findByName",
                query = "SELECT m FROM MethodAccessTestBean m WHERE m.name = :name"),
    })
@Entity
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

    public MethodAccessTestBean(int id, MethodAccessTestBean parent) {
        identifier = id;
        parentBean = parent;
    }

    @Id
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

    @ManyToOne
    public MethodAccessTestBean getParent() {
        return parentBean;
    }

    public void setParent(MethodAccessTestBean parent) {
        parentBean = parent;
    }

    @OneToMany
    public List<MethodAccessTestBean> getChildren() {
        return childBeans;
    }

    public void setChildren(List<MethodAccessTestBean> children) {
        childBeans = children;
    }

    @OneToMany
    public Map<MethodAccessTestBean, MethodAccessTestBean> getRelated() {
        return map;
    }

    public void setRelated(Map<MethodAccessTestBean, MethodAccessTestBean> related) {
        map = related;
    }
}
