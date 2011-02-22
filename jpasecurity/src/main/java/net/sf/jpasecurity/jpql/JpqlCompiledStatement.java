/*
 * Copyright 2008 - 2010 Arne Limburg
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
package net.sf.jpasecurity.jpql;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;

/**
 * This class represents compiled JPQL statements.
 * It contains methods to access the structure of a JPQL statement.
 * @author Arne Limburg
 */
public class JpqlCompiledStatement extends JpqlStatementHolder {

    private List<String> selectedPaths;
    private Set<TypeDefinition> typeDefinitions;
    private Set<String> namedParameters;

    public JpqlCompiledStatement(Node statement,
                                 List<String> selectedPathes,
                                 Set<TypeDefinition> typeDefinitions,
                                 Set<String> namedParameters) {
        super(statement);
        this.selectedPaths = selectedPathes;
        this.typeDefinitions = typeDefinitions;
        this.namedParameters = namedParameters;
    }

    public JpqlCompiledStatement(Node statement) {
        this(statement,
             Collections.<String>emptyList(),
             Collections.<TypeDefinition>emptySet(),
             Collections.<String>emptySet());
    }

    /**
     * Returns the paths of the select-clause.
     */
    public List<String> getSelectedPaths() {
        return selectedPaths;
    }

    /**
     * Returns the types of the selected paths.
     * @param mappingInformation the mapping information to determine the types
     * @return the types
     * @see #getSelectedPaths()
     */
    public Map<String, Class<?>> getSelectedTypes(MappingInformation mappingInformation) {
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: getSelectedPaths()) {
            selectedTypes.put(selectedPath, mappingInformation.getType(selectedPath, getTypeDefinitions()));
        }
        return selectedTypes;
    }

    /**
     * Returns the type-definitions of the from-clause and join-clauses.
     */
    public Set<TypeDefinition> getTypeDefinitions() {
        return typeDefinitions;
    }

    public Set<String> getNamedParameters() {
        return namedParameters;
    }

    public JpqlCompiledStatement clone() {
        return (JpqlCompiledStatement)super.clone();
    }
}
