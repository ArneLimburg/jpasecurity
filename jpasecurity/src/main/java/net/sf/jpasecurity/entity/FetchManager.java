/*
 * Copyright 2009 Arne Limburg
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
package net.sf.jpasecurity.entity;

/**
 * @author Arne Limburg
 */
public interface FetchManager {

    String MAX_FETCH_DEPTH = "net.sf.jpasecurity.maxFetchDepth";

    int getMaximumFetchDepth();

    /**
     * Fetches the object-graph of the specified entity up to the specified fetch depth.
     * @param entity the entity to fetch
     * @param depth the depth up to which to fetch
     */
    void fetch(Object entity, int depth);

}
