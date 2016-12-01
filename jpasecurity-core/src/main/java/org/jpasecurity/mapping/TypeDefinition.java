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
package org.jpasecurity.mapping;

/**
 * This class holds type-definitions of JPQL statements.
 * Types may be defined either in the from-clause or in join-clauses.
 * @author Arne Limburg
 */
public class TypeDefinition {

    private Alias alias;
    private Class<?> keyType;
    private Class<?> type;
    private Path joinPath;
    private boolean innerJoin;
    private boolean fetchJoin;

    public TypeDefinition(Alias alias, Class<?> type) {
        this.alias = alias;
        this.type = type;
    }

    public TypeDefinition(Class<?> keyType, Class<?> type, Path joinPath, boolean innerJoin, boolean fetchJoin) {
        this(null, keyType, type, joinPath, innerJoin, fetchJoin);
    }

    public TypeDefinition(Alias alias, Class<?> type, Path joinPath, boolean innerJoin) {
        this(alias, null, type, joinPath, innerJoin);
    }

    public TypeDefinition(Alias alias, Class<?> keyType, Class<?> type, Path joinPath, boolean innerJoin) {
        this(alias, keyType, type, joinPath, innerJoin, false);
    }

    public TypeDefinition(Alias alias, Class<?> type, Path joinPath, boolean innerJoin, boolean fetchJoin) {
        this(alias, null, type, joinPath, innerJoin, fetchJoin);
    }

    public TypeDefinition(Alias alias,
                          Class<?> keyType,
                          Class<?> type,
                          Path joinPath,
                          boolean innerJoin,
                          boolean fetchJoin) {
        this(alias, type);
        if (joinPath == null) {
            throw new IllegalArgumentException("joinPath may not be null");
        }
        this.joinPath = joinPath;
        this.innerJoin = innerJoin;
        this.fetchJoin = fetchJoin;
        this.keyType = keyType;
    }

    public Alias getAlias() {
        return alias;
    }

    public Path getJoinPath() {
        return joinPath;
    }

    public boolean isJoin() {
        return getJoinPath() != null;
    }

    public boolean isInnerJoin() {
        return isJoin() && innerJoin;
    }

    public boolean isOuterJoin() {
        return isJoin() && !innerJoin;
    }

    public boolean isFetchJoin() {
        return fetchJoin;
    }

    public boolean isPreliminary() {
        return type == null;
    }

    public <T> Class<T> getKeyType() {
        return (Class<T>)keyType;
    }

    public <T> Class<T> getType() {
        if (isPreliminary()) {
            throw new IllegalStateException("type is not yet determined");
        }
        return (Class<T>)type;
    }

    public void setType(Class<?> type) {
        if (!isPreliminary()) {
            throw new IllegalStateException("type already set");
        }
        this.type = type;
    }

    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        if (isInnerJoin()) {
            toStringBuilder.append("inner join ");
        } else if (isOuterJoin()) {
            toStringBuilder.append("outer join ");
        }
        if (isFetchJoin()) {
            toStringBuilder.append("fetch ");
        }
        if (isPreliminary()) {
            toStringBuilder.append("<undetermined type>");
        } else {
            toStringBuilder.append(type.getName());
        }
        if (alias != null) {
            toStringBuilder.append(' ').append(alias);
        }
        return toStringBuilder.toString();
    }
}
