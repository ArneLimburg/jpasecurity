/*
 * Copyright 2011 Stefan Hildebrandt
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
package org.jpasecurity.model.acl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_group")
public class Group extends AbstractEntity {
    private String name;

    private String description;

    private String groupType;

    @ManyToMany
    @JoinTable(name = "GROUP_HIERARCHY",
               joinColumns = @JoinColumn(name = "PARENT_ID"),
               inverseJoinColumns = @JoinColumn(name = "CHILD_ID"))
    private List<Group> fullHierarchy = new ArrayList<Group>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Group> getFullHierarchy() {
        return fullHierarchy;
    }

    public void setFullHierarchy(List<Group> fullHierarchy) {
        this.fullHierarchy = fullHierarchy;
    }
}
