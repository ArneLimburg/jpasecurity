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
package net.sf.jpasecurity.model.client;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Arne Limburg
 */
@Entity
public class Client {
    @Id
    @GeneratedValue
    private int id;
    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<ClientStaffing> staffing = new ArrayList<ClientStaffing>();
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Fetch(FetchMode.JOIN)
    private ClientStatus currentStatus;

    public int getId() {
        return id;
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
}
