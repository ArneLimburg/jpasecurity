/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.jpql;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Path;
import org.jpasecurity.jpql.parser.Node;

/**
 * This class represents compiled JPQL statements.
 * It contains methods to access the structure of a JPQL statement.
 * @author Arne Limburg
 */
public class JpqlCompiledStatement extends JpqlStatementHolder {

    private Class<?> constructorArgReturnType;
    private List<Path> selectedPaths;
    private Set<TypeDefinition> typeDefinitions;
    private Set<String> namedParameters;

    public JpqlCompiledStatement(Node statement,
                                 Class<?> constructorArgReturnType,
                                 List<Path> selectedPathes,
                                 Set<TypeDefinition> typeDefinitions,
                                 Set<String> namedParameters) {
        super(statement);
        this.constructorArgReturnType = constructorArgReturnType;
        this.selectedPaths = selectedPathes;
        this.typeDefinitions = typeDefinitions;
        this.namedParameters = namedParameters;
    }

    public JpqlCompiledStatement(Node statement) {
        this(statement,
             null,
             Collections.<Path>emptyList(),
             Collections.<TypeDefinition>emptySet(),
             Collections.<String>emptySet());
    }

    /**
     * If this query represents a constructor-arg syntax,
     * returns the java class of the result, otherwise returns <tt>null</tt>
     * @return the java class of the result or <tt>null</tt>
     */
    public Class<?> getConstructorArgReturnType() {
        return constructorArgReturnType;
    }

    /**
     * Returns the paths of the select-clause.
     */
    public List<Path> getSelectedPaths() {
        return selectedPaths;
    }

    /**
     * Returns the types of the selected paths.
     * @param metamodel the mapping information to determine the types
     * @return the types
     * @see #getSelectedPaths()
     */
    public Map<Path, Class<?>> getSelectedTypes(Metamodel metamodel) {
        Map<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        for (Path selectedPath: getSelectedPaths()) {
            selectedTypes.put(selectedPath,
                    TypeDefinition.Filter.managedTypeForPath(selectedPath)
                        .withMetamodel(metamodel)
                        .filter(getTypeDefinitions())
                        .getJavaType());
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
