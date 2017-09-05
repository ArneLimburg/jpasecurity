/*
 * Copyright 2008 - 2010 Arne Limburg
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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.jpasecurity.collection.SecureCollections;

/**
 * @author Arne Limburg
 */
@NamedQueries({
    @NamedQuery(name = "findAll", query = "select bean from FieldAccessAnnotationTestBean bean"),
    @NamedQuery(name = "findById", query = "select bean from FieldAccessAnnotationTestBean bean where bean.id = :id"),
    @NamedQuery(name = "findEmbeddableById",
        query = "select bean.embeddable from FieldAccessAnnotationTestBean bean where bean.id = :id")
    })
@NamedQuery(name = "findByName", query = "select bean from FieldAccessAnnotationTestBean bean where bean.name = :name")
@NamedNativeQueries({
    @NamedNativeQuery(
        name = "findAllNative",
        query =
            "select "
                + "bean.identifier identifier, "
                + "bean.beanName beanName, "
                + "bean.parentBean parentBean"
                + " from FieldAccessAnnotationTestBean bean",
        resultClass = FieldAccessAnnotationTestBean.class)
    })
@Entity
public class FieldAccessAnnotationTestBean {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "identifier")
    private int id;
    @Column(name = "beanName")
    private String name;
    @Embedded
    private SimpleEmbeddable embeddable;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentBean")
    private FieldAccessAnnotationTestBean parent;
    @OneToMany(mappedBy = "parent",
               fetch = FetchType.LAZY,
               cascade = CascadeType.ALL,
               targetEntity = FieldAccessAnnotationTestBean.class)
    private List<Object> children = new ArrayList<Object>();
    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    @MapKey(name = "key")
    private Map<FieldAccessMapKey, FieldAccessMapValue> map = new HashMap<FieldAccessMapKey, FieldAccessMapValue>();
    @Transient
    private int namePropertyReadCount = 0;
    @Transient
    private int namePropertyWriteCount = 0;
    @Transient
    private int prePersistCount = 0;
    @Transient
    private int postPersistCount = 0;
    @Transient
    private int preRemoveCount = 0;
    @Transient
    private int postRemoveCount = 0;
    @Transient
    private int preUpdateCount = 0;
    @Transient
    private int postUpdateCount = 0;
    @Transient
    private int postLoadCount = 0;

    protected FieldAccessAnnotationTestBean() {
    }

    public FieldAccessAnnotationTestBean(String name) {
        this.name = name;
    }

    public FieldAccessAnnotationTestBean(String name, FieldAccessAnnotationTestBean parent) {
        this.name = name;
        this.parent = parent;
    }

    public int getIdentifier() {
        return id;
    }

    public void setIdentifier(int identifier) {
        id = identifier;
    }

    public String getBeanName() {
        namePropertyReadCount++;
        return name;
    }

    public void setBeanName(String beanName) {
        namePropertyWriteCount++;
        name = beanName;
    }

    public SimpleEmbeddable getSimpleEmbeddable() {
        return embeddable;
    }

    public void setSimpleEmbeddable(SimpleEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    public int getNamePropertyReadCount() {
        return namePropertyReadCount;
    }

    public int getNamePropertyWriteCount() {
        return namePropertyWriteCount;
    }

    public int getPrePersistCount() {
        return prePersistCount;
    }

    public int getPostPersistCount() {
        return postPersistCount;
    }

    public int getPreRemoveCount() {
        return preRemoveCount;
    }

    public int getPostRemoveCount() {
        return postRemoveCount;
    }

    public int getPreUpdateCount() {
        return preUpdateCount;
    }

    public int getPostUpdateCount() {
        return postUpdateCount;
    }

    public int getPostLoadCount() {
        return postLoadCount;
    }

    public FieldAccessAnnotationTestBean getParentBean() {
        return parent;
    }

    public void setParentBean(FieldAccessAnnotationTestBean parentBean) {
        parent = parentBean;
    }

    public List<FieldAccessAnnotationTestBean> getChildBeans() {
        return SecureCollections.secureList((List<FieldAccessAnnotationTestBean>)(List<?>)children);
    }

    public void setChildren(List<Object> childBeans) {
        children = childBeans;
    }

    public Map<FieldAccessMapKey, FieldAccessMapValue> getValues() {
        return map;
    }

    public void aBusinessMethodThatDoesNothing() {
    }

    @PrePersist
    public void prePersistLifecycleMethod() {
        prePersistCount++;
    }

    @PostPersist
    public void postPersistLifecycleMethod() {
        postPersistCount++;
        if (postPersistCount != prePersistCount) {
            throw new IllegalStateException("postPersistCount(" + postPersistCount + ") != prePersistCount(" + prePersistCount + ")");
        }
    }

    @PreRemove
    public void preRemoveLifecycleMethod() {
        preRemoveCount++;
    }

    @PostRemove
    public void postRemoveLifecycleMethod() {
        postRemoveCount++;
        if (postRemoveCount != preRemoveCount) {
            throw new IllegalStateException("postRemoveCount(" + postRemoveCount + ") != preRemoveCount(" + preRemoveCount + ")");
        }
    }

    @PreUpdate
    public void preUpdateLifecycleMethod() {
        preUpdateCount++;
    }

    @PostUpdate
    public void postUpdateLifecycleMethod() {
        postUpdateCount++;
        if (postUpdateCount != preUpdateCount) {
            throw new IllegalStateException("postUpdateCount(" + postUpdateCount + ") != preUpdateCount(" + preUpdateCount + ")");
        }
    }

    @PostLoad
    public void postLoadLifecycleMethod() {
        postLoadCount++;
    }

    public int hashCode() {
        return id == 0? System.identityHashCode(this): id;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FieldAccessAnnotationTestBean)) {
            return false;
        }
        FieldAccessAnnotationTestBean bean = (FieldAccessAnnotationTestBean)object;
        if (id == 0) {
            return this == bean;
        }
        return id == bean.getIdentifier();
    }
}
