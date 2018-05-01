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

import org.jpasecurity.Alias;
import org.jpasecurity.Path;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.Collection;

import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;

/**
 * This class holds type-definitions of JPQL statements.
 * Types may be defined either in the from-clause or in join-clauses.
 *
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

    public TypeDefinition(Class<?> type, Path joinPath, boolean innerJoin, boolean fetchJoin) {
        this(null, null, type, joinPath, innerJoin, fetchJoin);
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

    @Override
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

    public abstract static class Filter<F extends Filter<?, T>, T> {

        protected Metamodel metamodel;

        public static AliasTypeFilter typeForAlias(Alias alias) {
            return new AliasTypeFilter(alias);
        }

        public static ManagedTypeFilter managedTypeForPath(Path path) {
            return new ManagedTypeFilter(path);
        }

        public static AttributeFilter attributeForPath(Path path) {
            return new AttributeFilter(path);
        }

        public F withMetamodel(Metamodel model) {
            metamodel = model;
            return (F)this;
        }

        public T filter(Collection<TypeDefinition> typeDefinitions) {
            for (TypeDefinition typeDefinition : typeDefinitions) {
                if (filter(typeDefinition)) {
                    return transform(typeDefinition);
                }
            }
            throw new PersistenceException("No type found for alias " + getFilterAlias());
        }

        protected abstract boolean filter(TypeDefinition typeDefinition);

        protected abstract T transform(TypeDefinition typeDefinition);

        protected abstract Alias getFilterAlias();
    }

    public static class AliasTypeFilter extends Filter<AliasTypeFilter, TypeDefinition> {

        private final Alias alias;

        public AliasTypeFilter(Alias alias) {
            this.alias = alias;
        }

        @Override
        protected boolean filter(TypeDefinition typeDefinition) {
            return alias.equals(typeDefinition.getAlias());
        }

        @Override
        protected TypeDefinition transform(TypeDefinition typeDefinition) {
            return typeDefinition;
        }

        @Override
        protected Alias getFilterAlias() {
            return alias;
        }
    }

    public static class AttributeFilter extends Filter<AttributeFilter, Attribute<?, ?>> {

        private Class<?> rootType;
        private String[] pathElements;

        public AttributeFilter(Path path) {
            pathElements = path.getPathComponents();
        }

        public AttributeFilter withRootType(Class<?> rootType) {
            this.rootType = rootType;
            String[] newPathElements = new String[pathElements.length + 1];
            System.arraycopy(pathElements, 0, newPathElements, 1, pathElements.length);
            pathElements = newPathElements;
            return this;
        }

        @Override
        protected boolean filter(TypeDefinition typeDefinition) {
            return new Alias(pathElements[0]).equals(typeDefinition.getAlias());
        }

        @Override
        protected Attribute<?, ?> transform(TypeDefinition typeDefinition) {
            rootType = typeDefinition.getType();
            return filter();
        }

        public Attribute<?, ?> filter() {
            Type<?> type = forModel(metamodel).filter(rootType);
            Attribute<?, ?> result = null;
            for (int i = 1; i < pathElements.length; i++) {
                if (!(type instanceof ManagedType)) {
                    throw new PersistenceException("Cannot navigate through simple property "
                            + pathElements[i] + " of type " + type.getJavaType());
                }
                result = ((ManagedType<?>)type).getAttribute(pathElements[i]);
                if (result.isCollection()) {
                    type = ((PluralAttribute<?, ?, ?>)result).getElementType();
                } else {
                    type = ((SingularAttribute<?, ?>)result).getType();
                }
            }
            return result;
        }

        @Override
        protected Alias getFilterAlias() {
            return new Alias(pathElements[0]);
        }
    }

    public static class ManagedTypeFilter extends Filter<ManagedTypeFilter, ManagedType<?>> {

        private final Path path;
        private final Filter<?, ?> filter;

        public ManagedTypeFilter(Path path) {
            this.path = path;
            if (path.hasSubpath()) {
                filter = new AttributeFilter(path);
            } else {
                filter = new AliasTypeFilter(path.getRootAlias());
            }
        }

        @Override
        public ManagedTypeFilter withMetamodel(Metamodel model) {
            filter.withMetamodel(model);
            return super.withMetamodel(model);
        }

        @Override
        protected boolean filter(TypeDefinition typeDefinition) {
            return filter.filter(typeDefinition);
        }

        @Override
        protected ManagedType<?> transform(TypeDefinition typeDefinition) {
            if (!path.hasSubpath()) {
                return forModel(metamodel).filter(typeDefinition.getType());
            }
            Attribute<?, ?> attribute = (Attribute<?, ?>)filter.transform(typeDefinition);
            if (attribute.isCollection()) {
                return (ManagedType<?>)((PluralAttribute<?, ?, ?>)attribute).getElementType();
            } else {
                return (ManagedType<?>)((SingularAttribute<?, ?>)attribute).getType();
            }
        }

        @Override
        protected Alias getFilterAlias() {
            return filter.getFilterAlias();
        }
    }
}
