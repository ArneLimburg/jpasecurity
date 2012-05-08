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
package net.sf.jpasecurity.entity;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureEntityTest extends AbstractSecureObjectTestCase {

// Disabled by problems in EntityLifecycleTest.commitReplacedCollection()
//    @Test
//    public void flushUntouched() {
//        getSecureEntity().flush();
//    }

    @Test
    public void flushTouched() {
        touch((Entity)getSecureEntity());

        expectUnsecureCopy(getSecureEntity(), getUnsecureEntity());
        replayUnsecureCopy(getSecureEntity(), getUnsecureEntity());

        getSecureEntity().flush();

        verifyUnsecureCopy(getSecureEntity(), getUnsecureEntity());
    }

    private void touch(Entity entity) {
        entity.isSecure();
    }
}
