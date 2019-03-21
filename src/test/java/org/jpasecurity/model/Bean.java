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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

/** @author Arne Limburg */
@Entity
@Inheritance
public class Bean {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Basic
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bean parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderColumn(name = "childIndex")
    private List<Bean> children = new ArrayList<>();

    @Transient
    private boolean preUpdate;

    public Bean() {
    }

    public Bean(String name) {
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

    public Bean getParent() {
        return parent;
    }

    public void setParent(Bean parent) {
        this.parent = parent;
    }

    public List<Bean> getChildren() {
        return children;
    }

    public void setChildren(List<Bean> children) {
        this.children = children;
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
        if (!(o instanceof Bean)) {
            return false;
        }

        Bean testBean = (Bean)o;
        return id != null && id.equals(testBean.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
