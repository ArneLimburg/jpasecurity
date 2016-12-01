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
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/** @author Arne Limburg */
@Entity
@Inheritance
public class TestBean {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Basic
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private TestBean parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<TestBean> children = new ArrayList<TestBean>();
    @ManyToMany
    private Map<TestBean, TestBean> related;
    @Transient
    private boolean preUpdate;

    public TestBean() {
    }

    public TestBean(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int identifier) {
        id = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TestBean getParent() {
        return parent;
    }

    public void setParent(TestBean parent) {
        this.parent = parent;
    }

    public List<TestBean> getChildren() {
        return children;
    }

    public void setChildren(List<TestBean> children) {
        this.children = children;
    }

    public Map<TestBean, TestBean> getRelated() {
        return related;
    }

    @PreUpdate
    private void preUpdate() {
        preUpdate = true;
    }

    public boolean isPreUpdate() {
        return preUpdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof TestBean)) {
            return false;
        }

        TestBean testBean = (TestBean)o;
        return id != null && id.equals(testBean.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
