/*
 * Copyright 2011 - 2017 Arne Limburg
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
package org.jpasecurity.persistence.security;

import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.compiler.SubselectEvaluator;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.EntityFilter;
import org.jpasecurity.security.FilterResult;
import org.jpasecurity.util.Types;

/**
 * This class extends {@link EntityFilter} by the ability to filter
 * @link {@link CriteriaQuery}s.
 *
 * @author Arne Limburg
 */
public class CriteriaEntityFilter extends EntityFilter {

    private final CriteriaVisitor criteriaVisitor;

    public CriteriaEntityFilter(Metamodel metamodel,
                                SecurePersistenceUnitUtil util,
                                CriteriaBuilder criteriaBuilder,
                                Collection<AccessRule> accessRules,
                                SubselectEvaluator... evaluators) throws ParseException {
        super(metamodel, util, accessRules, evaluators);
        criteriaVisitor = new CriteriaVisitor(metamodel, criteriaBuilder);
    }

    public <R> FilterResult<CriteriaQuery<R>> filterQuery(CriteriaQuery<R> query) {
        Selection<R> selection = query.getSelection();
        Map<Path, Class<?>> selectedTypes = new HashMap<>();
        if (selection == null) {
            for (Root<?> selectedRoot: query.getRoots()) {
                if (selectedRoot.getAlias() != null) {
                    selectedTypes.put(new Path(selectedRoot.getAlias()), selectedRoot.getJavaType());
                }
            }
        } else if (!selection.isCompoundSelection()) {
            Path path = getSelectedPath(0, selection);
            Class<?> selectedType = getSelectedType(selection);
            if (!Types.isSimplePropertyType(selectedType)) {
                selectedTypes.put(path, selectedType);
            }
        } else {
            List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
            for (int i = 0; i < compoundSelectionItems.size(); i++) {
                Selection<?> s = compoundSelectionItems.get(i);
                Path path = getSelectedPath(i, s);
                Class<?> selectedType = getSelectedType(s);
                if (!Types.isSimplePropertyType(selectedType)) {
                    selectedTypes.put(path, selectedType);
                }
            }
        }
        if (selectedTypes.isEmpty()) {
            Iterator<Root<?>> rootIterator = query.getRoots().iterator();
            for (int i = 0; i < query.getRoots().size(); i++) {
                Root<?> root = rootIterator.next();
                String alias = "alias" + i;
                root.alias(alias);
                selectedTypes.put(new Path(alias), root.getJavaType());
            }
        }
        AccessDefinition accessDefinition = createAccessDefinition(selectedTypes, AccessType.READ, getAliases(query));
        FilterResult<CriteriaQuery<R>> filterResult
            = getAlwaysEvaluatableResult(new JpqlCompiledStatement(null), query, accessDefinition);
        if (filterResult != null) {
            return filterResult;
        }
        optimize(accessDefinition);
        Set<String> parameterNames = compiler.getNamedParameters(accessDefinition.getAccessRules());
        Map<String, Object> parameters = accessDefinition.getQueryParameters();
        parameters.keySet().retainAll(parameterNames);

        CriteriaHolder criteriaHolder = new CriteriaHolder(query);
        getQueryPreparator().createWhere(accessDefinition.getAccessRules()).visit(criteriaVisitor, criteriaHolder);
        return new CriteriaFilterResult<>(
                query, parameters.size() > 0 ? parameters : null, query.getResultType(), criteriaHolder.getParameters()
        );
    }

    public FilterResult<CriteriaUpdate> filterQuery(CriteriaUpdate query) {
        return filterQuery(query, query.getRoot());
    }

    public FilterResult<CriteriaDelete> filterQuery(CriteriaDelete query) {
        return filterQuery(query, query.getRoot());
    }

    private <Q extends CommonAbstractCriteria> FilterResult<Q> filterQuery(Q query, Root<?> root) {
        Map<Path, Class<?>> selectedTypes = new HashMap<>();
        Path path = getSelectedPath(0, root);
        selectedTypes.put(path, root.getJavaType());
        AccessDefinition accessDefinition = createAccessDefinition(
                selectedTypes,
                AccessType.READ,
                root.getAlias() != null? singleton(new Alias(root.getAlias())): Collections.<Alias>emptySet());
        FilterResult<Q> filterResult
            = getAlwaysEvaluatableResult(new JpqlCompiledStatement(null), query, accessDefinition);
        if (filterResult != null) {
            return filterResult;
        }
        optimize(accessDefinition);
        Set<String> parameterNames = compiler.getNamedParameters(accessDefinition.getAccessRules());
        Map<String, Object> parameters = accessDefinition.getQueryParameters();
        parameters.keySet().retainAll(parameterNames);

        CriteriaHolder criteriaHolder = new CriteriaHolder(query);
        getQueryPreparator().createWhere(accessDefinition.getAccessRules()).visit(criteriaVisitor, criteriaHolder);
        return new CriteriaFilterResult<>(
                query, parameters.size() > 0 ? parameters : null, criteriaHolder.getParameters());
    }

    private Set<Alias> getAliases(AbstractQuery<?> query) {
        Set<Alias> aliases = new HashSet<>();
        for (Root<?> root: query.getRoots()) {
            if (root.getAlias() != null) {
                aliases.add(new Alias(root.getAlias()));
            }
        }
        return aliases;
    }

    private Path getSelectedPath(int index, Selection<?> selection) {
        if (selection instanceof Expression) {
            return getSelectedPath(index, (Expression<?>)selection);
        }
        if (selection.getAlias() == null) {
            selection.alias("alias" + index);
        }
        return new Path(selection.getAlias());
    }

    private Path getSelectedPath(int index, Expression<?> expression) {
        if (expression instanceof javax.persistence.criteria.Path) {
            return getSelectedPath(index, (javax.persistence.criteria.Path<?>)expression);
        }
        if (expression.getAlias() == null) {
            expression.alias("alias" + index);
        }
        return new Path(expression.getAlias());
    }

    private Path getSelectedPath(int index, javax.persistence.criteria.Path<?> path) {
        if (path.getParentPath() != null) {
            Type<?> type = getType(path.getModel());
            if (type.getPersistenceType() == PersistenceType.BASIC
                || type.getPersistenceType() == PersistenceType.EMBEDDABLE) {
                return getSelectedPath(index, path.getParentPath());
            }
            return getSelectedPath(index, path.getParentPath()).append(getName(path.getModel()));
        }
        if (path.getAlias() == null) {
            path.alias("alias" + index);
        }
        return new Path(path.getAlias());
    }

    private Class<?> getSelectedType(Selection<?> selection) {
        if (selection instanceof Expression) {
            return getSelectedType((Expression<?>)selection);
        }
        return selection.getJavaType();
    }

    private Class<?> getSelectedType(Expression<?> expression) {
        if (expression instanceof javax.persistence.criteria.Path) {
            return getSelectedType((javax.persistence.criteria.Path<?>)expression);
        }
        return expression.getJavaType();
    }

    private Class<?> getSelectedType(javax.persistence.criteria.Path<?> path) {
        Type<?> type = getType(path.getModel());
        if (type.getPersistenceType() == PersistenceType.BASIC
            || type.getPersistenceType() == PersistenceType.EMBEDDABLE) {
            return getSelectedType(path.getParentPath());
        } else {
            return type.getJavaType();
        }
    }

    private Type<?> getType(Bindable<?> bindable) {
        if (bindable.getBindableType() == BindableType.SINGULAR_ATTRIBUTE) {
            SingularAttribute<?, ?> attribute = (SingularAttribute<?, ?>)bindable;
            return attribute.getType();
        } else if (bindable.getBindableType() == BindableType.PLURAL_ATTRIBUTE) {
            PluralAttribute<?, ?, ?> attribute = (PluralAttribute<?, ?, ?>)bindable;
            return attribute.getElementType();
        } else { //bindable.getBindableType == BindableType.ENTITY_TYPE
            return (EntityType<?>)bindable;
        }
    }

    private String getName(Bindable<?> bindable) {
        if (bindable.getBindableType() == BindableType.ENTITY_TYPE) {
            EntityType<?> entityType = (EntityType<?>)bindable;
            return entityType.getName();
        } else {
            Attribute<?, ?> attribute = (Attribute<?, ?>)bindable;
            return attribute.getName();
        }
    }
}
