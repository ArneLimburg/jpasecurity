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
package net.sf.jpasecurity.security;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.mapping.TypeDefinition;

/**
 * @author Arne Limburg
 */
public class FilterResult<Q> {

    private Q query;
    private Map<String, Object> parameters;
    private List<String> selectedPaths;
    private Set<TypeDefinition> types;

    public FilterResult() {
    }

    public FilterResult(Q query) {
        this.query = query;
    }

    public FilterResult(Q query,
                        Map<String, Object> parameters,
                        List<String> selectedPaths,
                        Set<TypeDefinition> types) {
        this(query);
        this.parameters = parameters;
        this.selectedPaths = selectedPaths;
        this.types = types;
    }

    public Q getQuery() {
        return query;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public List<String> getSelectedPaths() {
        return selectedPaths;
    }

    public Set<TypeDefinition> getTypeDefinitions() {
        return types;
    }
}
