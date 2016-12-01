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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class Acl extends AbstractEntity {

    @OneToMany(mappedBy = "accessControlList", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<AclEntry> entries = new LinkedList<AclEntry>();

    public List<AclEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<AclEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(">>>\n");
        for (AclEntry aclEntry : entries) {
            result.append(aclEntry.toString());
            result.append("\n");
        }
        result.append("<<<\n");
        return result.toString();
    }
}
