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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 *
 * @author jose
 */
@Entity
@DiscriminatorValue(value = ProcessDefinitionDiscriminator.VALUE_ID_CLIENT)
public class ClientTask extends ProcessTaskInstance {

    @Transient
    private Client client;

    public Client getClient() {
        // Without JPASecurity the below get method returns a list
        if (getProcessInstanceProcessTaskInstances() != null && !getProcessInstanceProcessTaskInstances().isEmpty()) {
            ClientProcessInstance processInstance = (ClientProcessInstance)
                    getProcessInstanceProcessTaskInstances().get(0).getProcessInstance();
            if (processInstance != null) {
                return processInstance.getClient();
            }
        }
        return client;
    }

}
