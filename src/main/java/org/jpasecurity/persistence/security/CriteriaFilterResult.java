/*
 * Copyright 2012 Oliver Zhou
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
package org.jpasecurity.persistence.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Parameter;

import org.jpasecurity.security.FilterResult;

/**
 * @author Oliver Zhou
 */
public class CriteriaFilterResult<Q> extends FilterResult<Q> {

    private List<Parameter<?>> criteriaParameters = new ArrayList<Parameter<?>>();

    public CriteriaFilterResult(Q query, Map<String, Object> parameters, List<Parameter<?>> criteriaParameters) {
        super(query, parameters, null, null, null);
        this.criteriaParameters = criteriaParameters;
    }

    public CriteriaFilterResult(Q query,
            Map<String, Object> parameters, Class<?> constructorArgReturnType, List<Parameter<?>> criteriaParameters) {
        super(query, parameters, constructorArgReturnType, null, null);
        this.criteriaParameters = criteriaParameters;
    }

    public List<Parameter<?>> getCriteriaParameters() {
        return criteriaParameters;
    }
}
