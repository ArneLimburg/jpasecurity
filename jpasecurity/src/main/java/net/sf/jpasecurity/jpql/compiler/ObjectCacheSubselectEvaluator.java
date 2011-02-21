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
package net.sf.jpasecurity.jpql.compiler;

import java.util.Collection;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlExists;

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
                                  QueryEvaluationParameters<Collection<?>> parameters)
            throws NotEvaluatableException {
        if (!(subselect.getStatement().jjtGetParent() instanceof JpqlExists)) {
            parameters.setResultUndefined();
            throw new NotEvaluatableException("ObjectCacheSubselectEvaluator only can evaluate subselects of an EXISTS");
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

    protected Collection<?> getResult(Replacement replacement, QueryEvaluationParameters<Collection<?>> parameters)
            throws NotEvaluatableException {
        if (replacement.getReplacement() == null) {
            return objectCache.getSecureObjects(replacement.getTypeDefinition().getType());
        } else {
            return super.getResult(replacement, parameters);
        }
    }
}
