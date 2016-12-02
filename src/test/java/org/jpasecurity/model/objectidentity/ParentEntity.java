/*
 * Copyright 2012 Stefan Hildebrandt
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
package org.jpasecurity.model.objectidentity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class ParentEntity extends EntitySuperclass {

    @OneToOne
    private ChildEntityType1 oneToOne;

    @OneToOne(fetch = FetchType.LAZY)
    private ChildEntityType1 oneToOneLazy;

    @OneToOne(fetch = FetchType.LAZY)
    private EntitySuperclass oneToOneAbstractLazy;

    @OneToMany
    @JoinColumn(name = "CHILD_ENTITY")
    private List<ChildEntityType1> childEntities = new ArrayList<ChildEntityType1>();

    @OneToMany
    @JoinColumn(name = "CHILD_ENTITY2")
    private List<EntitySuperclass> abstractChildEntities = new ArrayList<EntitySuperclass>();

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ENTITY3")
    private List<EntitySuperclass> abstractLazyChildEntities = new ArrayList<EntitySuperclass>();

    public ChildEntityType1 getOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(ChildEntityType1 oneToOne) {
        this.oneToOne = oneToOne;
    }

    public ChildEntityType1 getOneToOneLazy() {
        return oneToOneLazy;
    }

    public void setOneToOneLazy(ChildEntityType1 oneToOneLazy) {
        this.oneToOneLazy = oneToOneLazy;
    }

    public List<ChildEntityType1> getChildEntities() {
        return childEntities;
    }

    public void setChildEntities(List<ChildEntityType1> childEntities) {
        this.childEntities = childEntities;
    }

    public List<EntitySuperclass> getAbstractChildEntities() {
        return abstractChildEntities;
    }

    public void setAbstractChildEntities(List<EntitySuperclass> abstractChildEntities) {
        this.abstractChildEntities = abstractChildEntities;
    }

    public List<EntitySuperclass> getAbstractLazyChildEntities() {
        return abstractLazyChildEntities;
    }

    public void setAbstractLazyChildEntities(List<EntitySuperclass> abstractLazyChildEntities) {
        this.abstractLazyChildEntities = abstractLazyChildEntities;
    }

    public EntitySuperclass getOneToOneAbstractLazy() {
        return oneToOneAbstractLazy;
    }

    public void setOneToOneAbstractLazy(EntitySuperclass oneToOneAbstractLazy) {
        this.oneToOneAbstractLazy = oneToOneAbstractLazy;
    }
}
