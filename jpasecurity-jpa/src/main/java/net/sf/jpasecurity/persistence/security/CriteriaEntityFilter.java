/*
 * Copyright 2011 - 2012 Arne Limburg
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
package net.sf.jpasecurity.persistence.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.compiler.SubselectEvaluator;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.Types;

/**
 * This class extends {@link EntityFilter} by the ability to filter
 * @link {@link CriteriaQuery}s.
 *
 * @author Arne Limburg
 */
public class CriteriaEntityFilter extends EntityFilter {

    private final CriteriaVisitor criteriaVisitor;

    public CriteriaEntityFilter(SecureObjectCache objectCache,
                                MappingInformation mappingInformation,
                                SecurityContext securityContext,
                                CriteriaBuilder criteriaBuilder,
                                ExceptionFactory exceptionFactory,
                                Collection<AccessRule> accessRules,
                                SubselectEvaluator... evaluators) {
        super(objectCache, mappingInformation, securityContext, exceptionFactory, accessRules, evaluators);
        criteriaVisitor = new CriteriaVisitor(mappingInformation, criteriaBuilder, securityContext);
    }

    public <R> FilterResult<CriteriaQuery<R>> filterQuery(CriteriaQuery<R> query) {
        Selection<R> selection = query.getSelection();
        Map<Path, Class<?>> selectedTypes = new HashMap<Path, Class<?>>();
        if (selection == null) {
            for (Root<?> selectedRoot: query.getRoots()) {
                if (selectedRoot.getAlias() != null) {
                    selectedTypes.put(new Path(selectedRoot.getAlias()), selectedRoot.getJavaType());
                }
            }
        } else if (!selection.isCompoundSelection()) {
            Path path = getPath(0, selection);
            if (path.hasSubpath() || !Types.isSimplePropertyType(selection.getJavaType())) {
                selectedTypes.put(path, selection.getJavaType());
            }
        } else {
            List<Selection<?>> compoundSelectionItems = selection.getCompoundSelectionItems();
            for (int i = 0; i < compoundSelectionItems.size(); i++) {
                Selection<?> s = compoundSelectionItems.get(i);
                Path path = getPath(i, s);
                if (path.hasSubpath() || !Types.isSimplePropertyType(s.getJavaType())) {
                    selectedTypes.put(path, s.getJavaType());
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
        AccessDefinition accessDefinition = createAccessDefinition(selectedTypes, AccessType.READ);
        FilterResult<CriteriaQuery<R>> filterResult = getAlwaysEvaluatableResult(query, accessDefinition);
        if (filterResult != null) {
            return filterResult;
        }
        optimize(accessDefinition);
        Set<String> parameterNames = compiler.getNamedParameters(accessDefinition.getAccessRules());
        Map<String, Object> parameters = accessDefinition.getQueryParameters();
        parameters.keySet().retainAll(parameterNames);

        CriteriaHolder criteriaHolder = new CriteriaHolder(query);
        getQueryPreparator().createWhere(accessDefinition.getAccessRules()).visit(criteriaVisitor, criteriaHolder);
        return new CriteriaFilterResult<CriteriaQuery<R>>(
                query, parameters.size() > 0? parameters: null, criteriaHolder.getParameters());
    }

    private Path getPath(int index, Selection<?> selection) {
        if (selection instanceof Expression) {
            return getPath(index, (Expression<?>)selection);
        }
        if (selection.getAlias() == null) {
            selection.alias("alias" + index);
        }
        return new Path(selection.getAlias());
    }

    private Path getPath(int index, Expression<?> expression) {
        if (expression instanceof javax.persistence.criteria.Path) {
            return getPath(index, (javax.persistence.criteria.Path<?>)expression);
        }
        if (expression.getAlias() == null) {
            expression.alias("alias" + index);
        }
        return new Path(expression.getAlias());
    }

    private Path getPath(int index, javax.persistence.criteria.Path<?> path) {
        if (path.getParentPath() != null) {
            return getPath(index, path.getParentPath()).append(((Attribute<?, ?>)path.getModel()).getName());
        }
        if (path.getAlias() == null) {
            path.alias("alias" + index);
        }
        return new Path(path.getAlias());
    }
}
