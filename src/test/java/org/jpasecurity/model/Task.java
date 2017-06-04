/*
 * Copyright 2011 Arne Limburg
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

import static org.jpasecurity.AccessType.CREATE;
import static org.jpasecurity.AccessType.DELETE;
import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.AccessType.UPDATE;
import static org.jpasecurity.util.Validate.notNull;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.jpasecurity.security.Permit;
import org.jpasecurity.security.PermitAny;

@Entity
@PermitAny({
    @Permit(access = { CREATE, READ }, where = "status = org.jpasecurity.model.TaskStatus.OPEN"),
    @Permit(access = UPDATE, where = "status = org.jpasecurity.model.TaskStatus.CLOSED"),
    @Permit(access = DELETE, where = "status <> org.jpasecurity.model.TaskStatus.OPEN")
    })
public class Task {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    @Enumerated
    private TaskStatus status;

    protected Task() {
    }

    public Task(String name) {
        this.name = notNull("name", name);
        this.status = TaskStatus.OPEN;
    }

    public String getName() {
        return name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || object.getClass() != getClass()) {
            return false;
        }
        Task task = (Task)object;
        return id == task.id;
    }
}
