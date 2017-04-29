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
package org.jpasecurity.persistence.mapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

public final class ManagedTypeFilter {

    private Metamodel metamodel;

    public static ManagedTypeFilter forModel(Metamodel model) {
        return new ManagedTypeFilter(model);
    }

    private ManagedTypeFilter(Metamodel model) {
        metamodel = model;
    }

    public EntityType<?> filter(String name) {
        for (EntityType<?> type: metamodel.getEntities()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public ManagedType<?> filter(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type not found");
        }
        try {
            return metamodel.managedType(type);
        } catch (IllegalArgumentException original) {
            // hibernate bug! Manged types don't contain embeddables
            try {
                return metamodel.embeddable(type);
            } catch (IllegalArgumentException e) {
                if (type.getSuperclass() == Object.class) {
                    throw original;
                }
                try {
                    return filter(type.getSuperclass()); // handles proxy classes
                } catch (IllegalArgumentException e2) {
                    throw original;
                }
            }
        }
    }

    public EntityType<?> filterEntity(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type not found");
        }
        try {
            return metamodel.entity(type);
        } catch (IllegalArgumentException original) {
            try {
                return filterEntity(type.getSuperclass()); // handles proxy classes
            } catch (IllegalArgumentException e) {
                throw original;
            }
        }
    }

    public Collection<ManagedType<?>> filterAll(Class<?> type) {
        Set<ManagedType<?>> filteredTypes = new HashSet<ManagedType<?>>();
        for (ManagedType<?> managedType: metamodel.getManagedTypes()) {
            if (type.isAssignableFrom(managedType.getJavaType())) {
                filteredTypes.add(managedType);
            }
        }
        return filteredTypes;
    }

    public Collection<EntityType<?>> filterEntities(Class<?> type) {
        Set<EntityType<?>> filteredTypes = new HashSet<EntityType<?>>();
        for (ManagedType<?> managedType: metamodel.getManagedTypes()) {
            if (type.isAssignableFrom(managedType.getJavaType()) && (managedType instanceof EntityType)) {
                filteredTypes.add((EntityType<?>)managedType);
            }
        }
        return filteredTypes;
    }
}
