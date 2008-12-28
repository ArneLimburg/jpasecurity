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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.mapping.AliasDefinition;
import net.sf.jpasecurity.util.ProxyInvocationHandler;


/**
 * An invocation handler to handle invocations on queries.
 * @author Arne Limburg
 */
public class QueryInvocationHandler extends ProxyInvocationHandler<Query> {

    private SecureObjectManager objectManager;
    private List<String> selectedPaths;
    private Set<AliasDefinition> aliases;
    private PathEvaluator pathEvaluator;

    public QueryInvocationHandler(SecureObjectManager objectManager,
                                  Query query,
                                  List<String> selectedPaths,
                                  Set<AliasDefinition> aliases,
                                  PathEvaluator pathEvaluator) {
        super(query);
        this.objectManager = objectManager;
        this.selectedPaths = selectedPaths;
        this.aliases = aliases;
        this.pathEvaluator = pathEvaluator;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = super.invoke(proxy, method, args);
        return getTarget().equals(result)? proxy: result;
    }

    public Object getSingleResult() {
        return getSecureResult(getTarget().getSingleResult());
    }

    public List getResultList() {
        List targetResult = getTarget().getResultList();
        List proxyResult = new ArrayList();
        for (Object entity: targetResult) {
            proxyResult.add(getSecureResult(entity));
        }
        return proxyResult;
    }

    private Object getSecureResult(Object result) {
        if (!(result instanceof Object[])) {
            result = objectManager.getSecureObject(result);
            if (selectedPaths != null) {
                executeFetchPlan(result, selectedPaths.get(0));
            }
            return result;
        }
        Object[] scalarResult = (Object[])result;
        for (int i = 0; i < scalarResult.length; i++) {
            scalarResult[i] = objectManager.getSecureObject(scalarResult[i]);
            if (selectedPaths != null) {
                executeFetchPlan(scalarResult[i], selectedPaths.get(i));
            }
        }
        return scalarResult;
    }

    private void executeFetchPlan(Object entity, String selectedPath) {
        selectedPath = resolveAliases(selectedPath);
        for (AliasDefinition aliasDefinition: aliases) {
            if (aliasDefinition.isFetchJoin()) {
                String fetchPath = resolveAliases(aliasDefinition.getJoinPath());
                if (fetchPath.startsWith(selectedPath)) {
                    pathEvaluator.evaluate(entity, fetchPath.substring(selectedPath.length() + 1));
                }
            }
        }
    }

    private String resolveAliases(String aliasedPath) {
        int index = aliasedPath.indexOf('.');
        String alias, path;
        if (index == -1) {
            alias = aliasedPath;
            path = null;
        } else {
            alias = aliasedPath.substring(0, index);
            path = aliasedPath.substring(index + 1);
        }
        for (AliasDefinition aliasDefinition: aliases) {
            if (aliasDefinition.getAlias().equals(alias)) {
                if (aliasDefinition.getJoinPath() == null) {
                    return aliasedPath;
                }
                return resolveAliases(aliasDefinition.getJoinPath() + '.' + path);
            }
        }
        throw new IllegalStateException("alias '" + alias + "' not found in alias definitions");
    }
}
