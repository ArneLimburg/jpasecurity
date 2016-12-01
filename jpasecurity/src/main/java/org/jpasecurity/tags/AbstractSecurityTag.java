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
package org.jpasecurity.tags;

import java.util.Enumeration;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.jpasecurity.AccessManager;

/**
 * Baseclass for tags that do access-checks.
 * @author Arne Limburg
 */
public abstract class AbstractSecurityTag extends TagSupport {

    public AccessManager getAccessManager() {

        AccessManager accessManager;

        accessManager = resolveAccessManager(PageContext.PAGE_SCOPE);
        if (accessManager != null) {
            return accessManager;
        }
        accessManager = resolveAccessManager(PageContext.REQUEST_SCOPE);
        if (accessManager != null) {
            return accessManager;
        }
        accessManager = resolveAccessManager(PageContext.SESSION_SCOPE);
        if (accessManager != null) {
            return accessManager;
        }
        accessManager = resolveAccessManager(PageContext.APPLICATION_SCOPE);
        if (accessManager != null) {
            return accessManager;
        }
        throw new IllegalStateException("No access manager defined for this page");
    }

    public int doStartTag() {
        if (!isAccessible()) {
            return Tag.SKIP_BODY;
        } else {
            return Tag.EVAL_BODY_INCLUDE;
        }
    }

    protected abstract boolean isAccessible();

    private AccessManager resolveAccessManager(int scope) {
        for (Enumeration<String> names = pageContext.getAttributeNamesInScope(scope); names.hasMoreElements();) {
            Object object = pageContext.getAttribute(names.nextElement(), scope);
            if (object instanceof AccessManager) {
                return (AccessManager)object;
            }
        }
        return null;
    }
}
