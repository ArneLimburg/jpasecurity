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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * @author Arne Limburg
 */
@Entity
public class MethodAccessAnnotationTestBean {

    private int identifier;
    private String beanName;
    private MethodAccessAnnotationTestBean parentBean;
    private List<MethodAccessAnnotationTestBean> childBeans;
    
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
        return beanName;
    }
    
    public void setName(String name) {
        beanName = name;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentBean")
    public MethodAccessAnnotationTestBean getParent() {
        return parentBean;
    }
    
    public void setParent(MethodAccessAnnotationTestBean parent) {
        parentBean = parent;
    }
    
    @OneToMany(mappedBy = "parent",
    		   fetch = FetchType.EAGER,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
    public List<MethodAccessAnnotationTestBean> getChildren() {
        return childBeans;
    }
    
    public void setChildren(List<MethodAccessAnnotationTestBean> children) {
        childBeans = children;
    }
}
