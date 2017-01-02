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
package org.jpasecurity.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;

public class DefaultSecurityContext implements SecurityContext {

    private Map<Alias, Object> values = new HashMap<Alias, Object>();

    @Override
    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(values.keySet());
    }

    @Override
    public Object getAliasValue(Alias alias) {
        return values.get(alias);
    }

    @Override
    public <T> Collection<T> getAliasValues(Alias alias) {
        Collection<T> collection = (Collection<T>)values.get(alias);
        return collection != null ? collection : Collections.<T>emptySet();
    }

    public void register(Alias alias, Object value) {
        values.put(alias, value);
    }

    public void unauthenticate() {
        values.clear();
    }
}
