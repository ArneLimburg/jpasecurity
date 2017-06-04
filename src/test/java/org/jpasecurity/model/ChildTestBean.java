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

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


/**
 * @author Stefan Hildebrandt
 */
@NamedQuery(name = "ChildTestBean.findAll", query = "SELECT c FROM ChildTestBean c")
@NamedQueries({
    @NamedQuery(name = "ChildTestBean.findById", query = "SELECT c FROM ChildTestBean c WHERE c.id = :id"),
    @NamedQuery(name = "ChildTestBean.findByName", query = "SELECT c FROM ChildTestBean c WHERE c.name = :name"),
    })
@Entity
public class ChildTestBean implements TestInterface {

    private int id;
    private String name;

    public ChildTestBean() {
    }

    public ChildTestBean(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
