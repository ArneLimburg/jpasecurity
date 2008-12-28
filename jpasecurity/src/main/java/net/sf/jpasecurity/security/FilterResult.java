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

import net.sf.jpasecurity.mapping.AliasDefinition;

/**
 * @author Arne Limburg
 */
public class FilterResult {

    private String query;
    private String userParameterName;
    private Map<String, Object> roleParameters;
    private List<String> selectedPaths;
    private Set<AliasDefinition> aliases;

    public FilterResult() {
    }

    public FilterResult(String query) {
        this.query = query;
    }

    public FilterResult(String query,
                        String userParameterName,
                        Map<String, Object> roleParameters,
                        List<String> selectedPaths,
                        Set<AliasDefinition> aliases) {
        this(query);
        this.userParameterName = userParameterName;
        this.roleParameters = roleParameters;
        this.selectedPaths = selectedPaths;
        this.aliases = aliases;
    }

    public String getQuery() {
        return query;
    }

    public String getUserParameterName() {
        return userParameterName;
    }

    public Map<String, Object> getRoleParameters() {
        return roleParameters;
    }

    public List<String> getSelectedPaths() {
        return selectedPaths;
    }

    public Set<AliasDefinition> getAliasDefinitions() {
        return aliases;
    }
}
