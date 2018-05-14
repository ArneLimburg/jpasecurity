/*
 * Copyright 2016 Arne Limburg
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
package org.jpasecurity.collection;

import java.util.List;

/**
 * This class consists exclusively of static methods that return
 * collections, that filter entities based on there accessibility regarding read access.
 * The returned collection is a wrapper around the specified collection,
 * that just contains the entities that the user may read.
 * Changes to the wrapper are reflected to the original collection.
 * <strong>Note</strong> that
 */
public final class SecureCollections {

    public static <E> List<E> secureList(List<E> list) {
        return new SecureList<>(list);
    }

    private SecureCollections() {
    }
}
