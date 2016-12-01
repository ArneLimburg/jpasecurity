/*
 * Copyright 2008 Arne Limburg
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

import javax.persistence.Basic;
import javax.persistence.Entity;

import net.sf.jpasecurity.security.Permit;

/**
 * @author Arne Limburg
 */
@Entity
@Permit(rule = "owner = CURRENT_PRINCIPAL")
public class TestBeanSubclass extends TestBean {

    @Basic
    private String owner;

    protected TestBeanSubclass() {
    }

    public TestBeanSubclass(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
