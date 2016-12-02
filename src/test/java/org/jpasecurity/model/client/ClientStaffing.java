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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Arne Limburg
 */
@Entity
public class ClientStaffing {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;
    private Date endDate;

    protected ClientStaffing() {
        //for jpa
    }

    public ClientStaffing(Client client, Employee employee) {
        this.client = client;
        this.employee = employee;
        this.client.getStaffing().add(this);
    }

    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Date getEndDate() {
        return endDate;
    }
}
