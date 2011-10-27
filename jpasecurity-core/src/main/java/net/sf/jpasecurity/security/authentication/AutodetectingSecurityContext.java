/*
 * Copyright 2008 - 2011 Arne Limburg
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
package net.sf.jpasecurity.security.authentication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.configuration.AuthenticationProvider;
import net.sf.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.ConfigurationReceiver;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.configuration.SecurityContextReceiver;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class tries to detect the security context for an application.
 * Internally it uses an {@link AuthenticationProviderSecurityContext}.
 * For that security context the following authentication providers are used in the specified order:
 * <ol>
 *   <li>
 *     If a <tt>org.springframework.security.context.SecurityContextHolder</tt> is present in the classpath,
 *     a {@link net.sf.jpasecurity.spring.authentication.SpringAuthenticationProvider} is used.
 *   </li>
 *   <li>
 *     If an <tt>javax.faces.context.FacesContext</tt> is present in the classpath,
 *     a {@link net.sf.jpasecurity.jsf.authentication} is used.
 *   </li>
 *   <li>
 *     If an <tt>javax.ejb.EJBContext</tt> is accessible via JNDI lookup,
 *     an {@link EjbAuthenticationProvider} is used.
 *   </li>
 *   <li>
 *     If none of the former conditions is true, a {@link DefaultAuthenticationProvider} is used.
 *   </li>
 * </ol>
 * @author Arne Limburg
 */
public class AutodetectingSecurityContext implements SecurityContext,
                                                     ConfigurationReceiver,
                                                     MappingInformationReceiver,
                                                     SecurityContextReceiver {

    private static final Log LOG = LogFactory.getLog(AutodetectingSecurityContext.class);

    private static final List<String> AUTHENTICATION_PROVIDER_CLASS_NAMES;
    static {
        List<String> authenticationProviderClassNames = new ArrayList<String>();
        authenticationProviderClassNames.add("net.sf.jpasecurity.spring.authentication.SpringAuthenticationProvider");
        authenticationProviderClassNames.add("net.sf.jpasecurity.security.authentication.EjbAuthenticationProvider");
        authenticationProviderClassNames.add("net.sf.jpasecurity.jsf.authentication.JsfAuthenticationProvider");
        AUTHENTICATION_PROVIDER_CLASS_NAMES = Collections.unmodifiableList(authenticationProviderClassNames);
    }

    private AuthenticationProviderSecurityContext securityContext;

    public AutodetectingSecurityContext() {
        securityContext = new AuthenticationProviderSecurityContext(autodetectAuthenticationProvider());
    }

    protected AuthenticationProvider autodetectAuthenticationProvider() {
        for (String providerClassName: AUTHENTICATION_PROVIDER_CLASS_NAMES) {
            try {
                ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
                Class<? extends AuthenticationProvider> authenticationProviderClass
                    = (Class<? extends AuthenticationProvider>)currentClassLoader.loadClass(providerClassName);
                LOG.info("autodetected presence of class " + providerClassName);
                AuthenticationProvider authenticationProvider = authenticationProviderClass.newInstance();
                LOG.info("using " + providerClassName);
                return authenticationProvider;
            } catch (IllegalAccessException e) {
                throw new SecurityException(e);
            } catch (ClassNotFoundException e) {
                //ignore and try next authentication provider
            } catch (InstantiationException e) {
                LOG.debug("could not instantiate class " + providerClassName, e);
            } catch (IllegalStateException e) {
                LOG.debug("constructor of class " + providerClassName + " threw exception", e);
            }
        }
        LOG.info("falling back to DefaultAuthenticationPovider");
        return new DefaultAuthenticationProvider();
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

    public void setConfiguration(Configuration configuration) {
        securityContext.setConfiguration(configuration);
    }

    public void setMappingInformation(MappingInformation persistenceMapping) {
        securityContext.setMappingInformation(persistenceMapping);
    }

    public void setMappingProperties(Map<String, Object> properties) {
        securityContext.setMappingProperties(properties);
    }

    public void setSecurityContext(SecurityContext newSecurityContext) {
        securityContext.setSecurityContext(newSecurityContext);
    }
}
