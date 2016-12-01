/*
 * Copyright 2010 - 2011 Arne Limburg
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
package org.jpasecurity.jpql.compiler;

import java.util.Collection;

import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.SecureEntity;
import org.jpasecurity.entity.SecureObjectCache;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.Node;

import org.jpasecurity.jpql.parser.JpqlExists;
import org.jpasecurity.jpql.parser.JpqlNoCacheIsAccessible;
import org.jpasecurity.jpql.parser.JpqlNoCacheQueryOptimize;
import org.jpasecurity.jpql.parser.JpqlSubselect;

/**
 * A subselect-evaluator that evaluates subselects based on the content of the object cache
 * @author Arne Limburg
 */
public class ObjectCacheSubselectEvaluator extends SimpleSubselectEvaluator {

    private final SecureObjectCache objectCache;

    public ObjectCacheSubselectEvaluator(SecureObjectCache objectCache, ExceptionFactory exceptionFactory) {
        super(exceptionFactory);
        if (objectCache == null) {
            throw new IllegalArgumentException("SecureObjectCache may not be null");
        }
        this.objectCache = objectCache;
    }

    public Collection<?> evaluate(JpqlCompiledStatement subselect,
                                  QueryEvaluationParameters parameters)
        throws NotEvaluatableException {
        if (!(isParentExists((JpqlSubselect)subselect.getStatement()))) {
            parameters.setResultUndefined();
            throw new NotEvaluatableException("ObjectCacheSubselectEvaluator only can evaluate subselects of an EXISTS");
        }
        if (isQueryOptimize(parameters)
            && !(canEvaluateInQueryOptimize((JpqlSubselect)subselect.getStatement(), parameters))) {
            parameters.setResultUndefined();
            throw new NotEvaluatableException(
                "ObjectCacheSubselectEvaluator is disabled by QUERY_OPTIMIZE_NOCACHE hint in mode " + parameters
                    .getEvaluationType());
        }
        if (isAccessCheck(parameters)
            && !(canEvaluateInAccessCheck((JpqlSubselect)subselect.getStatement(), parameters))) {
            parameters.setResultUndefined();
            throw new NotEvaluatableException(
                "ObjectCacheSubselectEvaluator is disabled by IS_ACCESSIBLE_NOCACHE hint in mode " + parameters
                    .getEvaluationType());
        }
        Collection<?> result = super.evaluate(subselect, parameters);
        if (result.size() > 0) {
            parameters.setResult(result);
            return result;
        } else {
            //We cannot know then whether there are objects
            //because that no objects could be found in the cache does not mean
            //that no object can be found in the database
            parameters.setResultUndefined();
            throw new NotEvaluatableException();
        }
    }

    public boolean canEvaluate(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return isParentExists(node)
            && (canEvaluateInQueryOptimize(node, parameters)
            || canEvaluateInAccessCheck(node, parameters));
    }

    private boolean canEvaluateInAccessCheck(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return isAccessCheck(parameters) && !isObjectCacheEvaluationDisabledByIsAccessibleHint(node);
    }

    private boolean canEvaluateInQueryOptimize(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return isQueryOptimize(parameters)
            && !isObjectCacheEvaluationDisabledByQueryHint(node);
    }

    private boolean isParentExists(JpqlSubselect node) {
        return node.jjtGetParent() instanceof JpqlExists;
    }

    private boolean isObjectCacheEvaluationDisabledByQueryHint(Node node) {
        return isEvaluationDisabledByHint(node, JpqlNoCacheQueryOptimize.class);
    }

    private boolean isObjectCacheEvaluationDisabledByIsAccessibleHint(Node node) {
        return isEvaluationDisabledByHint(node, JpqlNoCacheIsAccessible.class);
    }

    protected Collection<?> getResult(Replacement replacement, QueryEvaluationParameters parameters)
        throws NotEvaluatableException {
        if (replacement.getReplacement() == null) {
            Collection<?> secureObjects = objectCache.getSecureObjects(replacement.getTypeDefinition().getType());
            for (Object secureObject: secureObjects) {
                if (secureObject instanceof SecureEntity) {
                    SecureEntity secureEntity = (SecureEntity)secureObject;
                    if (!secureEntity.isInitialized()) {
                        ((SecureEntity)secureObject).refresh();
                    }
                }
            }
            return secureObjects;
        } else {
            return super.getResult(replacement, parameters);
        }
    }
}
