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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * @author Arne Limburg
 */
@NamedQueries({
  @NamedQuery(name = "findAll", query = "select bean from FieldAccessAnnotationTestBean bean"),
  @NamedQuery(name = "findById", query = "select bean from FieldAccessAnnotationTestBean bean where bean.id = :id")
})
@NamedQuery(name = "findByName", query = "select bean from FieldAccessAnnotationTestBean bean where bean.name = :name")
@Entity
public class FieldAccessAnnotationTestBean {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "identifier")
    private int id;
    @Column(name = "beanName")
    private String name;
    @ManyToOne
    @JoinColumn(name = "parentBean")
    private FieldAccessAnnotationTestBean parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<FieldAccessAnnotationTestBean> children = new ArrayList<FieldAccessAnnotationTestBean>();
    
    protected FieldAccessAnnotationTestBean() {
    }
    
    public FieldAccessAnnotationTestBean(String name) {
        this.name = name; 
    }
    
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
    
    public FieldAccessAnnotationTestBean getParentBean() {
        return parent;
    }
    
    public void setParentBean(FieldAccessAnnotationTestBean parentBean) {
        parent = parentBean;
    }
    
    public List<FieldAccessAnnotationTestBean> getChildBeans() {
        return children;
    }
    
    public void setChildren(List<FieldAccessAnnotationTestBean> childBeans) {
        children = childBeans;
    }
}
