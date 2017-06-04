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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Arne Limburg
 */
@Entity
@NamedQueries(
        @NamedQuery(name = Client.FIND_ALL_ID_AND_NAME, query =
        "SELECT new org.jpasecurity.dto.IdAndNameDto (a.id,a.name) FROM Client a ORDER BY a.name"))
public class Client extends AbstractEntity<Integer> {

    public static final String FIND_ALL_ID_AND_NAME = "Client.FIND_ALL_ID_AND_NAME";

    @ManyToOne(fetch = FetchType.LAZY)
    private Client parent;

    @ManyToOne(fetch = FetchType.LAZY)
    private ClientGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    private ClientStructure structure;

    @ManyToOne(fetch = FetchType.LAZY)
    private ClientType type;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE })
    private List<ClientStaffing> staffing = new ArrayList<ClientStaffing>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.JOIN)
    private ClientStatus currentStatus;

    private String anotherProperty;

    private String number;

    private String name;

    @PrimaryKeyJoinColumn
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private ClientOperationsTracking operationsTracking;

    public Client() {
    }

    public Client(String name) {
        this.name = name;
    }

    public Client getParent() {
        return parent;
    }

    public void setParent(Client parent) {
        this.parent = parent;
    }

    public ClientGroup getGroup() {
        return group;
    }

    public ClientStructure getStructure() {
        return structure;
    }

    public ClientType getType() {
        return type;
    }

    public ClientStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(ClientStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public List<ClientStaffing> getStaffing() {
        return staffing;
    }

    public String getAnotherProperty() {
        return anotherProperty;
    }

    public void setAnotherProperty(String value) {
        this.anotherProperty = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientOperationsTracking getOperationsTracking() {
        return operationsTracking;
    }

    public void setOperationsTracking(ClientOperationsTracking operationsTracking) {
        this.operationsTracking = operationsTracking;
    }
}
