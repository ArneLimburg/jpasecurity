/*
 * Copyright 2009 Arne Limburg
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
package net.sf.jpasecurity.util;

import javax.servlet.jsp.tagext.TagSupport;

import net.sf.jpasecurity.AccessManager;
import static net.sf.jpasecurity.AccessType.*;

/**
 * @author Arne Limburg
 */
public abstract class AccessUtils extends TagSupport {

    public static boolean canCreate(AccessManager accessManager, Object entity) {
        return accessManager.isAccessible(CREATE, entity);
    }

    public static boolean canRead(AccessManager accessManager, Object entity) {
        return accessManager.isAccessible(READ, entity);
    }

    public static boolean canUpdate(AccessManager accessManager, Object entity) {
        return accessManager.isAccessible(UPDATE, entity);
    }

    public static boolean canDelete(AccessManager accessManager, Object entity) {
        return accessManager.isAccessible(DELETE, entity);
    }
}
