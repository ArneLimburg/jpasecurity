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
package org.jpasecurity.model.client;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author jose
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "process_definition_discriminator_id", discriminatorType = DiscriminatorType.INTEGER)
public class ProcessTaskInstance extends AbstractEntity<Integer> {

    String description;

    Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_pti_assigned_employee_employee_id")
    @Fetch(FetchMode.JOIN)
    Employee assignedEmployee;

    @OneToMany(mappedBy = "processTaskInstance", fetch = FetchType.LAZY)
    private List<ProcessInstanceProcessTaskInstance> processInstanceProcessTaskInstances;

    public ProcessTaskInstance() {
    }

    public ProcessTaskInstance(String description, Integer sequence, Employee assignedEmployee) {
        this.description = description;
        this.sequence = sequence;
        this.assignedEmployee = assignedEmployee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Employee getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(Employee assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public List<ProcessInstanceProcessTaskInstance> getProcessInstanceProcessTaskInstances() {
        if (processInstanceProcessTaskInstances == null) {
            processInstanceProcessTaskInstances = new ArrayList<ProcessInstanceProcessTaskInstance>();
        }
        return processInstanceProcessTaskInstances;
    }

    public void setProcessInstanceProcessTaskInstances(List<ProcessInstanceProcessTaskInstance>
            processInstanceProcessTaskInstances) {
        this.processInstanceProcessTaskInstances = processInstanceProcessTaskInstances;
    }
}
