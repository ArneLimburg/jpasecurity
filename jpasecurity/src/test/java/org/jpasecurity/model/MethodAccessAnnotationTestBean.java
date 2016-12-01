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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * @author Arne Limburg
 */
@Entity
@EntityListeners(TestEntityListener.class)
public class MethodAccessAnnotationTestBean {

    private int identifier;
    private String beanName;
    private MethodAccessAnnotationTestBean parentBean;
    private List<MethodAccessAnnotationTestBean> childBeans = new ArrayList<MethodAccessAnnotationTestBean>();
    private int namePropertyReadCount = 0;
    private int namePropertyWriteCount = 0;

    public MethodAccessAnnotationTestBean() {
    }

    public MethodAccessAnnotationTestBean(String name) {
        beanName = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "identifier")
    public int getId() {
        return identifier;
    }

    public void setId(int id) {
        identifier = id;
    }

    @Column(name = "beanName")
    public String getName() {
        namePropertyReadCount++;
        return beanName;
    }

    public void setName(String name) {
        namePropertyWriteCount++;
        beanName = name;
    }

    @Transient
    public int getNamePropertyReadCount() {
        return namePropertyReadCount;
    }

    @Transient
    public int getNamePropertyWriteCount() {
        return namePropertyWriteCount;
    }

    public void aBusinessMethodThatDoesNothing() {
    }

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = MethodAccessAnnotationTestBean.class)
    @JoinColumn(name = "parentBean")
    public Object getParent() {
        return parentBean;
    }

    public void setParent(Object parent) {
        parentBean = (MethodAccessAnnotationTestBean)parent;
    }

    @OneToMany(mappedBy = "parent",
               fetch = FetchType.EAGER,
               cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH })
    public List<MethodAccessAnnotationTestBean> getChildren() {
        return childBeans;
    }

    public void setChildren(List<MethodAccessAnnotationTestBean> children) {
        childBeans = children;
    }

    public int hashCode() {
        return getId() == 0? System.identityHashCode(this): getId();
    }

    public boolean equals(Object object) {
        if (!(object instanceof MethodAccessAnnotationTestBean)) {
            return false;
        }
        MethodAccessAnnotationTestBean bean = (MethodAccessAnnotationTestBean)object;
        if (getId() == 0) {
            return this == bean;
        }
        return getId() == bean.getId();
    }
}
