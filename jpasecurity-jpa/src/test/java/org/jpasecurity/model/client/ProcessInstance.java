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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;

/**
 *
 * @author jose
 */
@Entity
@DiscriminatorColumn(name = "process_definition_discriminator_id", discriminatorType = DiscriminatorType.INTEGER)
public class ProcessInstance extends AbstractEntity<Integer> {

    Date effectiveDate;

    String definition;

    public ProcessInstance() {
    }

    public ProcessInstance(Date effectiveDate, String definition) {
        this.effectiveDate = effectiveDate;
        this.definition = definition;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
