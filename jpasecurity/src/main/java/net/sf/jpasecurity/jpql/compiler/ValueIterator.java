/*
 * Copyright 2008 - 2011 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ListHashMap;
import net.sf.jpasecurity.util.ListMap;
import net.sf.jpasecurity.util.SetMap;

/**
 * @author Arne Limburg
 */
public class ValueIterator implements Iterator<Map<Alias, Object>> {

    private final PathEvaluator pathEvaluator;

    private List<Alias> possibleAliases;
    private ListMap<Alias, Object> possibleValues;
    private ListMap<Alias, TypeDefinition> dependentTypeDefinitions;
    private Map<Alias, Object> currentValue;
    private ListMap<Alias, Object> currentPossibleDependentValues;
    private boolean initialized = false;

    public ValueIterator(SetMap<Alias, Object> possibleValues,
                         Set<TypeDefinition> typeDefinitions,
                         PathEvaluator pathEvaluator) {
        this.pathEvaluator = pathEvaluator;
        this.possibleAliases = new ArrayList<Alias>(possibleValues.keySet());
        this.possibleValues = new ListHashMap<Alias, Object>();
        this.dependentTypeDefinitions = new ListHashMap<Alias, TypeDefinition>();
        this.currentValue = new HashMap<Alias, Object>();
        this.currentPossibleDependentValues = new ListHashMap<Alias, Object>();
        for (Map.Entry<Alias, Set<Object>> possibleValueEntry: possibleValues.entrySet()) {
            this.possibleValues.put(possibleValueEntry.getKey(), new ArrayList<Object>(possibleValueEntry.getValue()));
        }
        for (TypeDefinition typeDefinition: getJoinAliasDefinitions(typeDefinitions)) {
            this.dependentTypeDefinitions.add(getAlias(typeDefinition.getJoinPath()), typeDefinition);
        }
    }

    public boolean hasNext() {
        if (!initialized) {
            if (possibleValues.isEmpty()) {
                return false;
            }
            for (Map.Entry<Alias, List<Object>> possibleAliasValueEntry: possibleValues.entrySet()) {
                if (possibleAliasValueEntry.getValue().isEmpty()) {
                    return false;
                }
                boolean possibleValueFound = false;
                for (Object possibleValue: possibleAliasValueEntry.getValue()) {
                    if (couldSetCurrentValues(possibleAliasValueEntry.getKey(), possibleValue)) {
                        possibleValueFound = true;
                        break;
                    }
                }
                if (!possibleValueFound) {
                    return false;
                }
            }
            return true;
        }
        if (currentValue.isEmpty()) {
            return false;
        }
        for (Alias alias: possibleAliases) {
            for (int i = possibleValues.indexOf(alias, currentValue.get(alias)) + 1;
                 i < possibleValues.size(alias);
                 i++) {
                if (couldSetCurrentValues(alias, possibleValues.get(alias, i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Alias, Object> next() {
        if (!initialized) {
            for (Alias possibleAlias: possibleAliases) {
                setCurrentValues(possibleAlias, possibleValues.get(possibleAlias, 0));
            }
            initialized = true;
            return new HashMap<Alias, Object>(currentValue);
        }
        for (Alias alias: possibleAliases) {
            Object current = currentValue.get(alias);
            if (dependentTypeDefinitions.containsKey(alias)) {
                for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
                    Object currentDependentValue = currentValue.get(dependentTypeDefinition.getAlias());
                    int index = currentPossibleDependentValues.indexOf(dependentTypeDefinition.getAlias(),
                                                                       currentDependentValue);
                    if (index == -1
                        || index == currentPossibleDependentValues.size(dependentTypeDefinition.getAlias()) - 1) {
                        currentPossibleDependentValues.remove(dependentTypeDefinition.getAlias());
                    } else {
                        Object nextValue
                            = currentPossibleDependentValues.get(dependentTypeDefinition.getAlias(), index + 1);
                        setCurrentValues(dependentTypeDefinition.getAlias(), nextValue);
                        return new HashMap<Alias, Object>(currentValue);
                    }
                }
            }
            int index = possibleValues.indexOf(alias, current);
            if (index == possibleValues.size(alias) - 1) {
                setCurrentValues(alias, possibleValues.get(alias, 0));
            } else {
                setCurrentValues(alias, possibleValues.get(alias, index + 1));
                return new HashMap<Alias, Object>(currentValue);
            }
        }
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void setCurrentValues(Alias alias, Object value) {
        Map<Alias, Object> currentValue = new HashMap<Alias, Object>(this.currentValue);
        ListMap<Alias, Object> currentPossibleDependentValues
            = new ListHashMap<Alias, Object>(this.currentPossibleDependentValues);
        if (couldSetCurrentValues(alias, value, currentValue, currentPossibleDependentValues)) {
            this.currentValue = currentValue;
            this.currentPossibleDependentValues = currentPossibleDependentValues;
        }
    }

    private boolean couldSetCurrentValues(Alias alias, Object value) {
        return couldSetCurrentValues(alias,
                        value,
                        new HashMap<Alias, Object>(currentValue),
                        new ListHashMap<Alias, Object>(currentPossibleDependentValues));
    }

    private boolean couldSetCurrentValues(Alias alias,
                    Object value,
                    Map<Alias, Object> currentValue,
                    ListMap<Alias, Object> currentPossibleDependentValues) {
        currentValue.put(alias, value);
        if (dependentTypeDefinitions.containsKey(alias)) {
            for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
                List<Object> dependentValues
                    = pathEvaluator.evaluateAll(Collections.singleton(value),
                                                getSubpath(dependentTypeDefinition.getJoinPath()));
                if (dependentValues.isEmpty() && dependentTypeDefinition.isInnerJoin()) {
                    return false;
                }
                currentPossibleDependentValues.put(dependentTypeDefinition.getAlias(), dependentValues);
                if (!couldSetCurrentValues(dependentTypeDefinition.getAlias(),
                                dependentValues.iterator().next(),
                                currentValue,
                                currentPossibleDependentValues)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Alias getAlias(String path) {
        int index = path.indexOf('.');
        return index == -1? new Alias(path): new Alias(path.substring(0, index));
    }

    private String getSubpath(String path) {
        int index = path.indexOf('.');
        return path.substring(index + 1);
    }

    private Set<TypeDefinition> getJoinAliasDefinitions(Set<TypeDefinition> typeDefinitions) {
        Set<TypeDefinition> joinTypeDefinitions = new HashSet<TypeDefinition>();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (typeDefinition.isJoin()) {
                joinTypeDefinitions.add(typeDefinition);
            }
        }
        return joinTypeDefinitions;
    }
}
