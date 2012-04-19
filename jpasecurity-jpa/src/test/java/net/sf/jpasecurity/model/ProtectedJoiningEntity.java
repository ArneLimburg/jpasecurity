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
package net.sf.jpasecurity.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
/** @author Stefan Hildebrandt */
public class ProtectedJoiningEntity {
    @Id
    private Integer id;

    private String field;

    @OneToOne
    private ProtectedJoinedEntity protectedJoinedEntity;

    @OneToOne
    private UnprotectedJoinedEntity unprotectedJoinedEntity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ProtectedJoinedEntity getProtectedJoinedEntity() {
        return protectedJoinedEntity;
    }

    public void setProtectedJoinedEntity(ProtectedJoinedEntity protectedJoinedEntity) {
        this.protectedJoinedEntity = protectedJoinedEntity;
    }

    public UnprotectedJoinedEntity getUnprotectedJoinedEntity() {
        return unprotectedJoinedEntity;
    }

    public void setUnprotectedJoinedEntity(UnprotectedJoinedEntity unprotectedJoinedEntity) {
        this.unprotectedJoinedEntity = unprotectedJoinedEntity;
    }
}
