/*
 * Copyright 2012 JPA Security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.model.client;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 *
 * @author Joe Amoros
 */
@Entity
public class ClientOperationsTracking implements Serializable {

    @Id
    @NotNull(message = "validation.mandatoryField")
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    private DataDeliveryMethod dataDeliveryMethod;

    private Boolean ftpInPlaceWithClient;

    private Boolean dataImportAutomated;

    public Integer getId() {
        return client != null ? client.getId(): null;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Boolean getFtpInPlaceWithClient() {
        return ftpInPlaceWithClient;
    }

    public void setFtpInPlaceWithClient(Boolean ftpInPlaceWithClient) {
        this.ftpInPlaceWithClient = ftpInPlaceWithClient;
    }

    public Boolean getDataImportAutomated() {
        return dataImportAutomated;
    }

    public void setDataImportAutomated(Boolean dataImportAutomated) {
        this.dataImportAutomated = dataImportAutomated;
    }

    public DataDeliveryMethod getDataDeliveryMethod() {
        return dataDeliveryMethod;
    }

    public void setDataDeliveryMethod(DataDeliveryMethod dataDeliveryMethod) {
        this.dataDeliveryMethod = dataDeliveryMethod;
    }
}
