/*
 * Copyright 2012 Arne Limburg
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
package org.jpasecurity.spring.acl;

import javax.persistence.Embeddable;

/**
 * @author Arne Limburg
 */
@Embeddable
public class Auditing {

    private boolean success;
    private boolean failure;

    protected Auditing() {
        //JPA requirement
    }

    public Auditing(boolean success, boolean failure) {
        this.success = success;
        this.failure = failure;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return failure;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Auditing)) {
            return false;
        }
        Auditing auditing = (Auditing)object;
        return isSuccess() == auditing.isSuccess() && isFailure() == auditing.isFailure();
    }

    public int hashCode() {
        return Boolean.valueOf(success).hashCode() ^ Boolean.valueOf(failure).hashCode();
    }
}
