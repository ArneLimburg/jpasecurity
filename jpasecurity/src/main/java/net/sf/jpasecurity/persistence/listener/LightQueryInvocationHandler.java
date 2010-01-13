/*
 * Copyright 2010 Stefan Hildebrandt
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
package net.sf.jpasecurity.persistence.listener;

import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

public class LightQueryInvocationHandler  extends ProxyInvocationHandler<Query> {

    private List<String> selectedPaths;
    private Set<TypeDefinition> types;
    private PathEvaluator pathEvaluator;

    public LightQueryInvocationHandler(
                                  Query query,
                                  List<String> selectedPaths,
                                  Set<TypeDefinition> types,
                                  PathEvaluator pathEvaluator) {
        super(query);
        this.selectedPaths = selectedPaths;
        this.types = types;
        this.pathEvaluator = pathEvaluator;
    }
}
