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
package net.sf.jpasecurity.mapping;

/**
 * @author Arne Limburg
 */
public class TypeDefinition {

    private String alias;
    private Class<?> type;
    private String joinPath;
    private boolean innerJoin;
    private boolean fetchJoin;

    public TypeDefinition(String alias, Class<?> type) {
        this.alias = alias;
        this.type = type;
    }

    public TypeDefinition(Class<?> type, String joinPath, boolean innerJoin, boolean fetchJoin) {
    	this(null, type, joinPath, innerJoin, fetchJoin);
    }

    public TypeDefinition(String alias, Class<?> type, String joinPath, boolean innerJoin, boolean fetchJoin) {
        this(alias, type);
        if (joinPath == null) {
            throw new IllegalArgumentException("joinPath may not be null");
        }
        this.joinPath = joinPath;
        this.innerJoin = innerJoin;
        this.fetchJoin = fetchJoin;
    }

    public String getAlias() {
        return alias;
    }

    public String getJoinPath() {
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

    public Class<?> getType() {
        return type;
    }
}
