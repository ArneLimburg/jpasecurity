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
package org.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jpasecurity.Alias;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.util.ListHashMap;
import org.jpasecurity.util.ListMap;
import org.jpasecurity.util.SetMap;

/**
 * @author Arne Limburg
 */
public class ValueIterator implements Iterator<Map<Alias, Object>> {

    private final PathEvaluator pathEvaluator;

    private List<Alias> possibleAliases;
    private ListMap<Alias, Object> possibleValues;
    private ListMap<Alias, TypeDefinition> dependentTypeDefinitions;
    private Map<Alias, Object> currentValues;
    private ListMap<Alias, Object> currentPossibleDependentValues;
    private boolean initialized = false;

    public ValueIterator(SetMap<Alias, Object> possibleValues,
                         Set<TypeDefinition> typeDefinitions,
                         PathEvaluator pathEvaluator) {
        this.pathEvaluator = pathEvaluator;
        this.possibleValues = new ListHashMap<Alias, Object>();
        this.dependentTypeDefinitions = new ListHashMap<Alias, TypeDefinition>();
        this.currentValues = new HashMap<Alias, Object>();
        this.currentPossibleDependentValues = new ListHashMap<Alias, Object>();
        for (TypeDefinition typeDefinition: getJoinAliasDefinitions(typeDefinitions)) {
            possibleValues.remove(typeDefinition.getAlias());
            this.dependentTypeDefinitions.add(typeDefinition.getJoinPath().getRootAlias(), typeDefinition);
        }
        for (Map.Entry<Alias, Set<Object>> possibleValueEntry: possibleValues.entrySet()) {
            this.possibleValues.put(possibleValueEntry.getKey(), new ArrayList<Object>(possibleValueEntry.getValue()));
        }
        this.possibleAliases = new ArrayList<Alias>(possibleValues.keySet());
    }

    public boolean hasNext() {
        if (!initialized) {
            return hasFirst();
        }
        for (Alias alias: possibleAliases) {
            if (hasNextDependentValue(alias)) {
                return true;
            }
            if (hasNextValue(alias)) {
                return true;
            }
        }
        return false;
    }

    public Map<Alias, Object> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (!initialized) {
            return new HashMap<Alias, Object>(first());
        }
        for (Alias alias: possibleAliases) {
            if (hasNextDependentValue(alias)) {
                return nextDependentValue(alias);
            }
        }
        for (Alias alias: possibleAliases) {
            if (hasNextValue(alias)) {
                return nextValue(alias);
            }
        }
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private boolean hasFirst() {
        if (possibleAliases.isEmpty()) {
            return false;
        }
        for (Alias alias: possibleAliases) {
            if (!hasFirstValue(alias)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasFirstValue(Alias alias) {
        if (possibleValues.size(alias) == 0) {
            return false;
        }
        for (Object value: possibleValues.get(alias)) {
            if (hasFirstDependentValues(alias, value)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFirstDependentValues(Alias alias, Object value) {
        if (!dependentTypeDefinitions.containsKey(alias)) {
            return true;
        }
        for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
            if (!hasFirstDependentValue(value, dependentTypeDefinition)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasFirstDependentValue(Object value, TypeDefinition dependentTypeDefinition) {
        if (dependentTypeDefinition.isOuterJoin()) {
            return true;
        }
        String subpath = dependentTypeDefinition.getJoinPath().getSubpath();
        List<Object> dependentValues = pathEvaluator.evaluateAll(Collections.singleton(value), subpath);
        if (dependentValues.isEmpty()) {
            return false;
        }
        for (Object dependentValue: dependentValues) {
            if (!hasFirstDependentValues(dependentTypeDefinition.getAlias(), dependentValue)) {
                return false;
            }
        }
        return true;
    }

    private Map<Alias, Object> first() {
        for (Alias alias: possibleAliases) {
            currentValues.put(alias, firstValue(alias));
        }
        initialized = true;
        return currentValues;
    }

    private Object firstValue(Alias alias) {
        for (Object value: possibleValues.get(alias)) {
            if (hasFirstDependentValues(alias, value)) {
                firstDependentValues(alias, value);
                return value;
            }
        }
        throw new NoSuchElementException();
    }

    private Map<Alias, Object> firstDependentValues(Alias alias, Object value) {
        if (!hasFirstDependentValues(alias, value)) {
            throw new NoSuchElementException();
        }
        if (!dependentTypeDefinitions.containsKey(alias)) {
            return currentValues;
        }
        for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
            currentValues.put(dependentTypeDefinition.getAlias(), firstDependentValue(value, dependentTypeDefinition));
        }
        return currentValues;
    }

    private Object firstDependentValue(Object value, TypeDefinition dependentTypeDefinition) {
        String subpath = dependentTypeDefinition.getJoinPath().getSubpath();
        List<Object> dependentValues = pathEvaluator.evaluateAll(Collections.singleton(value), subpath);
        currentPossibleDependentValues.put(dependentTypeDefinition.getAlias(), dependentValues);
        if (dependentValues.isEmpty()) {
            if (dependentTypeDefinition.isOuterJoin()) {
                return null;
            }
            throw new NoSuchElementException();
        }
        for (Object dependentValue: dependentValues) {
            if (hasFirstDependentValues(dependentTypeDefinition.getAlias(), dependentValue)) {
                firstDependentValues(dependentTypeDefinition.getAlias(), dependentValue);
                return dependentValue;
            }
        }
        throw new NoSuchElementException();
    }

    private boolean hasNextValue(Alias alias) {
        for (int i = possibleValues.indexOf(alias, currentValues.get(alias)) + 1; i < possibleValues.size(alias); i++) {
            if (hasFirstDependentValues(alias, possibleValues.get(alias, i))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNextDependentValue(Alias alias) {
        if (!dependentTypeDefinitions.containsKey(alias)) {
            return false;
        }
        for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
            Alias dependentAlias = dependentTypeDefinition.getAlias();
            if (hasNextDependentValue(dependentAlias)) {
                return true;
            }
            if (hasNextDependentValue(dependentAlias, currentValues.get(dependentAlias))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNextDependentValue(Alias dependentAlias, Object currentDependentValue) {
        if (currentDependentValue == null) { // outer join
            return false;
        }
        int currentIndex = currentPossibleDependentValues.indexOf(dependentAlias, currentDependentValue);
        int currentPossibleSize = currentPossibleDependentValues.size(dependentAlias);
        return currentIndex < currentPossibleSize - 1;
    }

    private Map<Alias, Object> nextValue(Alias alias) {
        Object currentValue = currentValues.get(alias);
        for (int i = possibleValues.indexOf(alias, currentValue) + 1; i < possibleValues.size(alias); i++) {
            Object nextValue = possibleValues.get(alias, i);
            if (hasFirstDependentValues(alias, nextValue)) {
                currentValues.put(alias, nextValue);
                firstDependentValues(alias, nextValue);
            }
        }
        int currentIndex = possibleAliases.indexOf(alias);
        for (int i = 0; i < currentIndex; i++) {
            Alias possibleAlias = possibleAliases.get(i);
            currentValues.put(possibleAlias, firstValue(possibleAlias));
        }
        for (int i = currentIndex + 1; i < possibleAliases.size(); i++) {
            Alias possibleAlias = possibleAliases.get(i);
            firstDependentValues(possibleAlias, currentValues.get(possibleAlias));
        }
        return currentValues;
    }

    private Map<Alias, Object> nextDependentValue(Alias alias) {
        if (!dependentTypeDefinitions.containsKey(alias)) {
            throw new NoSuchElementException();
        }
        for (TypeDefinition dependentTypeDefinition: dependentTypeDefinitions.get(alias)) {
            Alias dependentAlias = dependentTypeDefinition.getAlias();
            if (hasNextDependentValue(dependentAlias)) {
                return nextDependentValue(dependentAlias);
            }
            Object currentValue = currentValues.get(dependentAlias);
            if (hasNextDependentValue(dependentAlias, currentValue)) {
                return nextDependentValue(dependentAlias, currentValue);
            }
        }
        throw new NoSuchElementException();
    }

    private Map<Alias, Object> nextDependentValue(Alias dependentAlias, Object currentDependentValue) {
        int currentIndex = currentPossibleDependentValues.indexOf(dependentAlias, currentDependentValue);
        for (int i = currentIndex + 1; i < currentPossibleDependentValues.size(dependentAlias); i++) {
            Object nextDependentValue = currentPossibleDependentValues.get(dependentAlias, i);
            if (hasFirstDependentValues(dependentAlias, nextDependentValue)) {
                currentValues.put(dependentAlias, nextDependentValue);
                return firstDependentValues(dependentAlias, nextDependentValue);
            }
        }
        throw new NoSuchElementException();
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
