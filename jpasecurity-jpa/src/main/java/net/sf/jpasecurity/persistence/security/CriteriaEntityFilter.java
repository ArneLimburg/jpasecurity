/*
 * Copyright 2011 Arne Limburg
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
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
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
import net.sf.jpasecurity.security.EntityFilter;

/**
 * This class extends {@link EntityFilter} by the ability to filter
 * @link {@link CriteriaQuery}s.
 *
 * @author Arne Limburg
 */
public class CriteriaEntityFilter extends EntityFilter {

    private CriteriaVisitor criteriaVisitor;

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

    public <R> CriteriaQuery<R> filterQuery(CriteriaQuery<R> query) {
        Selection<R> selection = query.getSelection();
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        if (selection == null) {
            for (Root<?> selectedRoot: query.getRoots()) {
                if (selectedRoot.getAlias() != null) {
                    selectedTypes.put(selectedRoot.getAlias(), selectedRoot.getJavaType());
                }
            }
            if (selectedTypes.isEmpty()) {
                Iterator<Root<?>> rootIterator = query.getRoots().iterator();
                for (int i = 0; i < query.getRoots().size(); i++) {
                    Root<?> root = rootIterator.next();
                    String alias = "alias" + i;
                    root.alias(alias);
                    selectedTypes.put(alias, root.getJavaType());
                }
            }
        } else if (!selection.isCompoundSelection()) {
            selectedTypes.put(getPath(selection), selection.getJavaType());
        } else {
            for (Selection<?> s: selection.getCompoundSelectionItems()) {
                selectedTypes.put(getPath(s), s.getJavaType());
            }
        }
        AccessDefinition accessDefinition = createAccessDefinition(selectedTypes, AccessType.READ);
        CriteriaHolder criteriaHolder = new CriteriaHolder(query);
        accessDefinition.getAccessRules().visit(criteriaVisitor, criteriaHolder);
        return query;
    }

    private String getPath(Selection<?> selection) {
        if (selection instanceof Expression) {
            return getPath((Expression<?>)selection);
        } else if (selection.getAlias() != null) {
            return selection.getAlias();
        } else {
            throw new UnsupportedOperationException("Unsupported type of selection, please define an alias "
                                                    + selection.getClass().getSimpleName());
        }
    }

    private String getPath(Expression<?> expression) {
        if (expression instanceof Path) {
            return getPath((Path<?>)expression);
        } else {
            throw new UnsupportedOperationException("Unsupported type of selection, please define an alias "
                                                    + expression.getClass().getSimpleName());
        }
    }

    private String getPath(Path<?> path) {
        if (path.getParentPath() == null) {
            return path.getAlias();
        }
        return getPath(path.getParentPath()) + '.' + ((Attribute<?, ?>)path.getModel()).getName();
    }
}
