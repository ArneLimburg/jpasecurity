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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "AclEntry")
@Table(name = "aclentry")
public class AclEntry extends AbstractEntity {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "ACCESS_CONTROL_LIST")
    private Acl accessControlList;

    @ManyToOne
    @JoinColumn(name = "GROUP_FK")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "PRIVILEGE_FK")
    private Privilege privilege;

    @Column(name = "UPDATE_")
    private Boolean update = false;

    @Column(name = "READ_")
    private Boolean read = false;

    @Column(name = "DELETE_")
    private Boolean delete = false;

    public Acl getAccessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(Acl accessControlList) {
        this.accessControlList = accessControlList;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (read) {
            result.append("r");
        } else {
            result.append("-");
        }
        if (update) {
            result.append("u");
        } else {
            result.append("-");
        }
        if (delete) {
            result.append("d:'");
        } else {
            result.append("-:'");
        }
        if (group != null) {
            result.append(group.getName());
            result.append("'/'");
            result.append(group.getGroupType());
            result.append("'");
        }
        return result.toString();
    }

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }
}
