/*
 * Copyright 2010 Arne Limburg
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * @author Arne Limburg
 */
@SuppressWarnings("deprecation")
public class MockPageContext extends PageContext {

    private Map<Integer, Map<String, Object>> attributes = new HashMap<Integer, Map<String, Object>>();

    public MockPageContext() {
        attributes.put(PageContext.PAGE_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.REQUEST_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.SESSION_SCOPE, new HashMap<String, Object>());
        attributes.put(PageContext.APPLICATION_SCOPE, new HashMap<String, Object>());
    }

    public Object getAttribute(String name, int scope) {
        return attributes.get(scope).get(name);
    }

    public void removeAttribute(String name) {
        attributes.get(getAttributesScope(name)).remove(name);
    }

    public void removeAttribute(String name, int scope) {
        attributes.get(scope).remove(name);
    }

    public void setAttribute(String name, Object attribute) {
        setAttribute(name, attribute, PageContext.PAGE_SCOPE);
    }

    public void setAttribute(String name, Object o, int scope) {
        attributes.get(scope).put(name, o);
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PageContext.PAGE_SCOPE);
    }

    public Object findAttribute(String name) {
        Object attribute;
        attribute = getAttribute(name, PageContext.PAGE_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        attribute = getAttribute(name, PageContext.REQUEST_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        attribute = getAttribute(name, PageContext.SESSION_SCOPE);
        if (attribute != null) {
            return attribute;
        }
        return getAttribute(name, PageContext.APPLICATION_SCOPE);
    }

    public Enumeration<String> getAttributeNamesInScope(final int scope) {
        return new Enumeration<String>() {

            private Iterator<String> iterator = attributes.get(scope).keySet().iterator();

            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public String nextElement() {
                return iterator.next();
            }
        };
    }

    public int getAttributesScope(String name) {
        for (Map.Entry<Integer, Map<String, Object>> scope: attributes.entrySet()) {
            if (scope.getValue().containsKey(name)) {
                return scope.getKey();
            }
        }
        return 0;
    }

    public void forward(String relativeUrlPath) throws ServletException, IOException {
    }

    public Exception getException() {
        return null;
    }

    public JspWriter getOut() {
        return null;
    }

    public Object getPage() {
        return null;
    }

    public ServletRequest getRequest() {
        return null;
    }

    public ServletResponse getResponse() {
        return null;
    }

    public ServletConfig getServletConfig() {
        return null;
    }

    public ServletContext getServletContext() {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public void handlePageException(Exception e) throws ServletException, IOException {
    }

    public void handlePageException(Throwable arg0) throws ServletException, IOException {
    }

    public void include(String relativeUrlPath) throws ServletException, IOException {
    }

    public void initialize(Servlet servlet,
                           ServletRequest request,
                           ServletResponse response,
                           String errorPageURL,
                           boolean needsSession,
                           int bufferSize,
                           boolean autoFlush) throws IOException {
    }

    public void release() {
    }

    public void include(String arg0, boolean arg1) throws ServletException, IOException {
    }

    public ELContext getELContext() {
        return null;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }

    public VariableResolver getVariableResolver() {
        return null;
    }
}
