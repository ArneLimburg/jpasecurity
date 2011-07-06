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
package net.sf.jpasecurity.persistence;

import static net.sf.jpasecurity.util.JpaTypes.isSimplePropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaQuery;
import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.mapping.TypeDefinition;


/**
 * This class handles invocations on queries.
 * @author Arne Limburg
 */
public class SecureQuery extends DelegatingQuery {

    private SecureObjectManager objectManager;
    private FetchManager fetchManager;
    private List<String> selectedPaths;
    private Set<TypeDefinition> types;
    private PathEvaluator pathEvaluator;
    private FlushModeType flushMode;

    public SecureQuery(SecureObjectManager objectManager,
                       FetchManager fetchManager,
                       Query query,
                       List<String> selectedPaths,
                       Set<TypeDefinition> types,
                       PathEvaluator pathEvaluator,
                       FlushModeType flushMode) {
        super(query);
        this.objectManager = objectManager;
        this.fetchManager = fetchManager;
        this.selectedPaths = selectedPaths;
        this.types = types;
        this.pathEvaluator = pathEvaluator;
        this.flushMode = flushMode;
    }

    public Query setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return super.setFlushMode(flushMode);
    }

    public Query setParameter(int index, Object parameter) {
        return objectManager.setParameter(new JpaQuery(getDelegate()), index, parameter).getWrappedQuery();
    }

    public Query setParameter(String name, Object parameter) {
        return objectManager.setParameter(new JpaQuery(getDelegate()), name, parameter).getWrappedQuery();
    }

    public Object getSingleResult() {
        preFlush();
        Object result = getSecureResult(getDelegate().getSingleResult());
        postFlush();
        return result;
    }

    public List getResultList() {
        preFlush();
        List targetResult = super.getResultList();
        postFlush();
        List proxyResult = new ArrayList();
        for (Object entity: targetResult) {
            proxyResult.add(getSecureResult(entity));
        }
        return proxyResult;
    }

    private void preFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.preFlush();
        }
    }

    private void postFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.postFlush();
        }
    }

    private Object getSecureResult(Object result) {
        if (result == null) {
            return null;
        }
        if (isSimplePropertyType(result.getClass())) {
            return result;
        }
        if (!(result instanceof Object[])) {
            result = objectManager.getSecureObject(result);
            fetchManager.fetch(result);
            return result;
        }
        Object[] scalarResult = (Object[])result;
        for (int i = 0; i < scalarResult.length; i++) {
            if (scalarResult[i] != null && !isSimplePropertyType(scalarResult[i].getClass())) {
                scalarResult[i] = objectManager.getSecureObject(scalarResult[i]);
                if (selectedPaths != null) {
                    fetchManager.fetch(scalarResult[i]);
                }
            }
        }
        return scalarResult;
    }
}
