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
package org.jpasecurity.persistence;

import javax.persistence.spi.LoadState;
import javax.persistence.spi.ProviderUtil;

public class EmptyProviderUtil implements ProviderUtil {

    static final ProviderUtil INSTANCE = new EmptyProviderUtil();

    @Override
    public LoadState isLoaded(Object entity) {
        return LoadState.UNKNOWN;
    }

    @Override
    public LoadState isLoadedWithReference(Object entity, String property) {
        return LoadState.UNKNOWN;
    }

    @Override
    public LoadState isLoadedWithoutReference(Object entity, String property) {
        return LoadState.UNKNOWN;
    }
}
