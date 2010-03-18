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
package net.sf.jpasecurity.entity;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.jpasecurity.AccessManager;


/**
 * @author Arne Limburg
 */
public class DefaultSecureCollection<E, T extends Collection<E>> extends AbstractSecureCollection<E, T> {

    public DefaultSecureCollection(T target, AbstractSecureObjectManager objectManager, AccessManager accessManager) {
        super(target, objectManager, accessManager);
    }

    DefaultSecureCollection(T target, T filtered, AbstractSecureObjectManager objectManager) {
        super(target, filtered, objectManager);
    }

    protected T createFiltered() {
        return (T)new ArrayList<E>();
    }
}
