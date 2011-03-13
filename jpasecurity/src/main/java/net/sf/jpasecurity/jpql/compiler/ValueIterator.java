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
                boolean possibleValueFound = false;
                for (Object possibleValue: possibleAliasValueEntry.getValue()) {
                    if (canSetCurrentValues(possibleAliasValueEntry.getKey(), possibleValue)) {
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
            if (dependentTypeDefinitions.containsKey(alias)) {
                for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
                    Alias dependentAlias = dependentTypeDefinition.getAlias();
                    Object currentDependentValue = currentValue.get(dependentAlias);
                    if (currentDependentValue == null) {
                        //outer join
                        continue;
                    }
                    List<Object> possibleDependentValues = currentPossibleDependentValues.get(dependentAlias);
                    for (int i = possibleDependentValues.indexOf(currentDependentValue) + 1;
                         i < possibleDependentValues.size();
                         i++) {
                        if (canSetCurrentValues(dependentAlias, possibleDependentValues.get(i))) {
                            return true;
                        }
                    }
                }
            }
            for (int i = possibleValues.indexOf(alias, currentValue.get(alias)) + 1;
                 i < possibleValues.size(alias);
                 i++) {
                if (canSetCurrentValues(alias, possibleValues.get(alias, i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Alias, Object> next() {
        if (!initialized) {
            initialize();
            if (currentValue.isEmpty()) {
                throw new NoSuchElementException();
            }
            return new HashMap<Alias, Object>(currentValue);
        }
        for (Alias alias: possibleAliases) {
            Object current = currentValue.get(alias);
            if (dependentTypeDefinitions.containsKey(alias)) {
                for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
                    Object currentDependentValue = currentValue.get(dependentTypeDefinition.getAlias());
                    int index = currentPossibleDependentValues.indexOf(dependentTypeDefinition.getAlias(),
                                                                       currentDependentValue);
                    for (int i = index + 1;
                         i < currentPossibleDependentValues.size(dependentTypeDefinition.getAlias());
                         i++) {
                        Object nextValue
                            = currentPossibleDependentValues.get(dependentTypeDefinition.getAlias(), i);
                        Map<Alias, Object> currentValue = new HashMap<Alias, Object>(this.currentValue);
                        ListMap<Alias, Object> currentPossibleDepententValues
                            = new ListHashMap<Alias, Object>(this.currentPossibleDependentValues);
                        if (canSetCurrentValues(dependentTypeDefinition.getAlias(),
                                                nextValue,
                                                currentValue,
                                                currentPossibleDepententValues)) {
                            this.currentValue = currentValue;
                            this.currentPossibleDependentValues = currentPossibleDepententValues;
                            return new HashMap<Alias, Object>(currentValue);
                        }
                    }
                }
            }
            int index = possibleValues.indexOf(alias, current);
            if (index == possibleValues.size(alias) - 1) {
                for (int i = 0; i < index; i++) {
                    Map<Alias, Object> currentValue = new HashMap<Alias, Object>(this.currentValue);
                    ListMap<Alias, Object> currentPossibleDepententValues
                        = new ListHashMap<Alias, Object>(this.currentPossibleDependentValues);
                    if (canSetCurrentValues(alias,
                                            possibleValues.get(alias, i),
                                            currentValue,
                                            currentPossibleDepententValues)) {
                        this.currentValue = currentValue;
                        this.currentPossibleDependentValues = currentPossibleDepententValues;
                        break;
                    }
                }
            } else {
                for (int i = index + 1; i < possibleValues.size(alias); i++) {
                    Map<Alias, Object> currentValue = new HashMap<Alias, Object>(this.currentValue);
                    ListMap<Alias, Object> currentPossibleDepententValues
                        = new ListHashMap<Alias, Object>(this.currentPossibleDependentValues);
                    if (canSetCurrentValues(alias,
                                            possibleValues.get(alias, i),
                                            currentValue,
                                            currentPossibleDepententValues)) {
                        this.currentValue = currentValue;
                        this.currentPossibleDependentValues = currentPossibleDepententValues;
                        return new HashMap<Alias, Object>(currentValue);
                    }
                }
            }
        }
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void initialize() {
        for (Alias possibleAlias: possibleAliases) {
            boolean valueFound = false;
            for (Object possibleValue: possibleValues.get(possibleAlias)) {
                Map<Alias, Object> currentValue = new HashMap<Alias, Object>(this.currentValue);
                ListMap<Alias, Object> currentPossibleDependentValues
                    = new ListHashMap<Alias, Object>(this.currentPossibleDependentValues);
                if (canSetCurrentValues(possibleAlias, possibleValue, currentValue, currentPossibleDependentValues)) {
                    this.currentValue = currentValue;
                    this.currentPossibleDependentValues = currentPossibleDependentValues;
                    valueFound = true;
                    break;
                }
            }
            if (!valueFound) {
                throw new NoSuchElementException();
            }
        }
        initialized = true;
    }

    private boolean canSetCurrentValues(Alias alias, Object value) {
        return canSetCurrentValues(alias,
                                   value,
                                   new HashMap<Alias, Object>(currentValue),
                                   new ListHashMap<Alias, Object>(currentPossibleDependentValues));
    }

    private boolean canSetCurrentValues(Alias alias,
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
                    currentPossibleDependentValues.remove(dependentTypeDefinition.getAlias());
                    return false;
                }
                currentPossibleDependentValues.put(dependentTypeDefinition.getAlias(), dependentValues);
                if (dependentValues.isEmpty()) {
                    //must be outer join
                    currentValue.put(dependentTypeDefinition.getAlias(), null);
                    continue;
                }
                if (!canSetCurrentValues(dependentTypeDefinition.getAlias(),
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
