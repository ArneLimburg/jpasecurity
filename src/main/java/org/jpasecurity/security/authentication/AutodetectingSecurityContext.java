/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.security.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.DefaultSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tries to detect the security context for an application.
 * For that security context the following security contexts are used in the specified order:
 * <ol>
 *   <li>
 *     If CDI 1.1 is available in the classpath and it provides a bean of type
 *     <tt>org.jpasecurity.SecurityContext</tt>,
 *     a {@link org.jpasecurity.security.authentication.CdiSecurityContext} is used
 *     and calls are delegated to that bean.
 *   </li>
 *   <li>
 *     If an <tt>javax.faces.context.FacesContext</tt> is present in the classpath,
 *     a {@link org.jpasecurity.jsf.authentication.JsfSecurityContext} is used.
 *   </li>
 *   <li>
 *     If an <tt>javax.faces.context.FacesContext</tt> is present in the classpath,
 *     a {@link org.jpasecurity.jsf.authentication.JsfSecurityContext} is used.
 *   </li>
 *   <li>
 *     If an <tt>javax.ejb.EJBContext</tt> is accessible via JNDI lookup,
 *     an {@link EjbSecurityContext} is used.
 *   </li>
 *   <li>
 *     If none of the former conditions is true, a {@link DefaultAuthenticationProvider} is used.
 *   </li>
 * </ol>
 * @author Arne Limburg
 */
public class AutodetectingSecurityContext implements SecurityContext {

    private static final Logger LOG = LoggerFactory.getLogger(AutodetectingSecurityContext.class);

    private static final List<String> SECURITY_CONTEXT_CLASS_NAMES;
    static {
        List<String> authenticationProviderClassNames = new ArrayList<String>();
        authenticationProviderClassNames.add("org.jpasecurity.spring.authentication.SpringSecurityContext");
        authenticationProviderClassNames.add("org.jpasecurity.security.authentication.CdiSecurityContext");
        authenticationProviderClassNames.add("org.jpasecurity.jsf.authentication.JsfSecurityContext");
        authenticationProviderClassNames.add("org.jpasecurity.security.authentication.EjbSecurityContext");
        SECURITY_CONTEXT_CLASS_NAMES = Collections.unmodifiableList(authenticationProviderClassNames);
    }

    private SecurityContext securityContext;

    public AutodetectingSecurityContext() {
        securityContext = autodetectSecurityContext();
    }

    protected SecurityContext autodetectSecurityContext() {
        for (String providerClassName: SECURITY_CONTEXT_CLASS_NAMES) {
            try {
                ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                Class<? extends SecurityContext> securityContextClass
                    = (Class<? extends SecurityContext>)currentClassLoader.loadClass(providerClassName);
                LOG.info("autodetected presence of class " + providerClassName);
                SecurityContext securityContext = securityContextClass.newInstance();
                LOG.info("using " + providerClassName);
                return securityContext;
            } catch (NoClassDefFoundError e) {
                //ignore and try next authentication provider
            } catch (ReflectiveOperationException e) {
                //ignore and try next authentication provider
            }
        }
        LOG.info("falling back to DefaultAuthenticationPovider");
        return new DefaultSecurityContext();
    }

    public Collection<Alias> getAliases() {
        return securityContext.getAliases();
    }

    public Object getAliasValue(Alias alias) {
        return securityContext.getAliasValue(alias);
    }

    public <T> Collection<T> getAliasValues(Alias alias) {
        return securityContext.getAliasValues(alias);
    }
}
