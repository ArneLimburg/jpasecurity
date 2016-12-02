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

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
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
@DiscriminatorValue(value = ProcessDefinitionDiscriminator.VALUE_ID_CLIENT)
public class ClientProcessInstance extends ProcessInstance {

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_process_instance_client_id")
    @Fetch(FetchMode.JOIN)
    private Client client;

    public ClientProcessInstance() {
    }

    public ClientProcessInstance(Client client, Date effectiveDate, String definition) {
        super(effectiveDate, definition);
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
