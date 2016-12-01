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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ForeignKey;

/**
 *
 * @author jose
 */
@Entity
public class ProcessInstanceProcessTaskInstance extends AbstractEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FK_pipti_process_instance_id")
    @Fetch(FetchMode.JOIN)
    private ProcessInstance processInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "FK_pipti_pro_task_instance_id")
    @Fetch(FetchMode.JOIN)
    private ProcessTaskInstance processTaskInstance;

    public ProcessInstanceProcessTaskInstance() {
    }

    public ProcessInstanceProcessTaskInstance(ProcessInstance processInstance,
            ProcessTaskInstance processTaskInstance) {
        this.processInstance = processInstance;
        this.processTaskInstance = processTaskInstance;
        processTaskInstance.getProcessInstanceProcessTaskInstances().add(this);
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public ProcessTaskInstance getProcessTaskInstance() {
        return processTaskInstance;
    }

    public void setProcessTaskInstance(ProcessTaskInstance processTaskInstance) {
        this.processTaskInstance = processTaskInstance;
    }
}
