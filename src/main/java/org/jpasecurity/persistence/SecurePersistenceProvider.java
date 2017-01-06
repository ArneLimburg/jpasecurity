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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import javax.xml.xpath.XPathExpressionException;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.rules.AccessRulesProvider;

@SuppressWarnings("rawtypes")
public class SecurePersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = Logger.getLogger(SecurePersistenceProvider.class.getName());

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";
    static final String NATIVE_PERSISTENCE_PROVIDER_PROPERTY = "org.jpasecurity.persistence.provider";
    static final String SECURITY_CONTEXT_PROPERTY = "org.jpasecurity.security.context";
    static final String ACCESS_RULES_PROVIDER_PROPERTY = "org.jpasecurity.security.rules.provider";
    private static final String DEFAULT_ORM_XML_LOCATION = "META-INF/orm.xml";
    private static final String DEFAULT_SECURITY_CONTEXT_PROPERTY
        = "org.jpasecurity.security.authentication.AutodetectingSecurityContext";
    private static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS
        = "org.jpasecurity.security.rules.XmlAccessRulesProvider";

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo unitInfo, Map properties) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        PersistenceProvider nativePersistenceProvider = createNativePersistenceProvider(unitInfo, properties);
        if (nativePersistenceProvider == null) {
            return null;
        }
        EntityManagerFactory nativeFactory
            = nativePersistenceProvider.createContainerEntityManagerFactory(unitInfo, properties);
        List<String> ormXmlLocations = new ArrayList<String>(unitInfo.getMappingFileNames());
        ormXmlLocations.add(DEFAULT_ORM_XML_LOCATION);
        Class<? extends SecurityContext> securityContextType = createSecurityContextType(unitInfo, properties);
        Class<? extends AccessRulesProvider> accessRulesProviderType
            = createAccessRulesProviderType(unitInfo, properties);
        return new SecureEntityManagerFactory(unitInfo.getPersistenceUnitName(),
                                              nativeFactory,
                                              ormXmlLocations,
                                              securityContextType,
                                              accessRulesProviderType);
    }

    @Override
    public EntityManagerFactory createEntityManagerFactory(String unitName, Map properties) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        XmlParser xmlParser;
        try {
            Enumeration<URL> persistenceXmls
                = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
            xmlParser = new XmlParser(Collections.list(persistenceXmls));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not initialize xml parser", e);
            return null;
        }
        PersistenceProvider nativePersistenceProvider
            = createNativePersistenceProvider(unitName, properties, xmlParser);
        if (nativePersistenceProvider == null) {
            return null;
        }
        try {
            EntityManagerFactory nativeFactory
                = nativePersistenceProvider.createEntityManagerFactory(unitName, properties);
            List<String> ormXmlLocations = new ArrayList<String>(xmlParser.parseMappingFileNames(unitName));
            ormXmlLocations.add(DEFAULT_ORM_XML_LOCATION);
            Class<? extends SecurityContext> securityContextType
                = createSecurityContextType(unitName, properties, xmlParser);
            Class<? extends AccessRulesProvider> accessRulesProviderType
                = createAccessRulesProviderType(unitName, properties, xmlParser);
            return new SecureEntityManagerFactory(unitName,
                                                  nativeFactory,
                                                  ormXmlLocations,
                                                  securityContextType,
                                                  accessRulesProviderType);
        } catch (XPathExpressionException e) {
            LOG.log(Level.WARNING, "Could not parse mapping files", e);
            return null;
        }
    }

    public void generateSchema(PersistenceUnitInfo unitInfo, Map properties) {
        if (properties == null) {
            properties = new HashMap();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        PersistenceProvider nativePersistenceProvider = createNativePersistenceProvider(unitInfo, properties);
        if (nativePersistenceProvider == null) {
            return;
        }
        nativePersistenceProvider.generateSchema(unitInfo, properties);
    }

    public boolean generateSchema(String unitName, Map properties) {
        if (properties == null) {
            properties = new HashMap();
        } else {
            properties = new HashMap<String, Object>(properties);
        }
        PersistenceProvider nativePersistenceProvider = createNativePersistenceProvider(unitName, properties);
        if (nativePersistenceProvider == null) {
            return false;
        }
        return nativePersistenceProvider.generateSchema(unitName, properties);
    }

    @Override
    public ProviderUtil getProviderUtil() {
        return EmptyProviderUtil.INSTANCE;
    }

    private boolean isOtherPersistenceProvider(String persistenceProviderClassName) {
        return persistenceProviderClassName != null && !getClass().getName().equals(persistenceProviderClassName);
    }

    private boolean isOtherPersistenceProvider(String overriddenPersistenceProviderClassName,
            String persistenceProviderClassName) {
        return overriddenPersistenceProviderClassName == null
                && !getClass().getName().equals(persistenceProviderClassName);
    }

    private String getPersistenceProviderClassName(String unitName, XmlParser parser) {
        try {
            return parser.parsePersistenceProvider(unitName);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getNativePersistenceProviderClassName(String unitName,
                                                         String overriddenPersistenceProviderClassName,
                                                         String persistenceProviderClassName,
                                                         Map properties,
                                                         XmlParser xmlParser) {
        String nativePersistenceProviderClassName = (String)properties.get(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
        if (nativePersistenceProviderClassName != null) {
            return nativePersistenceProviderClassName;
        }
        if (overriddenPersistenceProviderClassName != null
            && isOtherPersistenceProvider(persistenceProviderClassName)) {
            return persistenceProviderClassName;
        } else {
            try {
                return xmlParser.parsePersistenceProperty(unitName, NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
            } catch (XPathExpressionException e) {
                return null;
            }
        }
    }

    private String getNativePersistenceProviderClassName(PersistenceUnitInfo persistenceUnitInfo,
                                                         String overriddenPersistenceProviderClassName,
                                                         Map properties) {
        String nativePersistenceProviderClassName = (String)properties.get(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
        if (nativePersistenceProviderClassName != null) {
            return nativePersistenceProviderClassName;
        }
        if (overriddenPersistenceProviderClassName != null
            && isOtherPersistenceProvider(persistenceUnitInfo.getPersistenceProviderClassName())) {
            return persistenceUnitInfo.getPersistenceProviderClassName();
        } else {
            return persistenceUnitInfo.getProperties().getProperty(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
        }
    }

    private Class<? extends SecurityContext> createSecurityContextType(String unit, Map properties, XmlParser parser)
        throws XPathExpressionException {
        String securityContextClassName = (String)properties.get(SECURITY_CONTEXT_PROPERTY);
        if (securityContextClassName == null) {
            securityContextClassName = parser.parsePersistenceProperty(unit, SECURITY_CONTEXT_PROPERTY);
        }
        if (securityContextClassName == null) {
            securityContextClassName = DEFAULT_SECURITY_CONTEXT_PROPERTY;
        }
        try {
            return (Class<? extends SecurityContext>)Thread.currentThread().getContextClassLoader()
                    .loadClass(securityContextClassName);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private Class<? extends SecurityContext> createSecurityContextType(PersistenceUnitInfo persistenceUnitInfo,
                                                                       Map properties) {
        String securityContextClassName = (String)properties.get(SECURITY_CONTEXT_PROPERTY);
        if (securityContextClassName == null) {
            securityContextClassName = (String)persistenceUnitInfo.getProperties().get(SECURITY_CONTEXT_PROPERTY);
        }
        if (securityContextClassName == null) {
            securityContextClassName = DEFAULT_SECURITY_CONTEXT_PROPERTY;
        }
        try {
            return (Class<? extends SecurityContext>)getClassLoader(persistenceUnitInfo)
                    .loadClass(securityContextClassName);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private Class<? extends AccessRulesProvider> createAccessRulesProviderType(String unit,
                                                                               Map properties,
                                                                               XmlParser parser)
        throws XPathExpressionException {
        String accessRulesProviderClassName = (String)properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
        if (accessRulesProviderClassName == null) {
            accessRulesProviderClassName = parser.parsePersistenceProperty(unit, ACCESS_RULES_PROVIDER_PROPERTY);
        }
        if (accessRulesProviderClassName == null) {
            accessRulesProviderClassName = DEFAULT_ACCESS_RULES_PROVIDER_CLASS;
        }
        try {
            return (Class<? extends AccessRulesProvider>)Thread.currentThread().getContextClassLoader()
                    .loadClass(accessRulesProviderClassName);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private Class<? extends AccessRulesProvider> createAccessRulesProviderType(PersistenceUnitInfo persistenceUnitInfo,
                                                                               Map properties) {
        String accessRulesProviderClassName = (String)properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
        if (accessRulesProviderClassName == null) {
            accessRulesProviderClassName
                = (String)persistenceUnitInfo.getProperties().get(ACCESS_RULES_PROVIDER_PROPERTY);
        }
        if (accessRulesProviderClassName == null) {
            accessRulesProviderClassName = DEFAULT_ACCESS_RULES_PROVIDER_CLASS;
        }
        try {
            return (Class<? extends AccessRulesProvider>)getClassLoader(persistenceUnitInfo)
                    .loadClass(accessRulesProviderClassName);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private PersistenceProvider createNativePersistenceProvider(PersistenceUnitInfo persistenceUnitInfo,
                                                                Map properties) {
        String overriddenPersistenceProviderClassName = (String)properties.get(PERSISTENCE_PROVIDER_PROPERTY);
        if (isOtherPersistenceProvider(overriddenPersistenceProviderClassName)) {
            return null;
        }
        String persistenceProviderClassName = persistenceUnitInfo.getPersistenceProviderClassName();
        if (isOtherPersistenceProvider(overriddenPersistenceProviderClassName, persistenceProviderClassName)) {
            return null;
        }
        String nativePersistenceProviderClassName
            = getNativePersistenceProviderClassName(persistenceUnitInfo,
                                                    overriddenPersistenceProviderClassName,
                                                    properties);
        try {
            if (nativePersistenceProviderClassName == null
                || nativePersistenceProviderClassName.equals(SecurePersistenceProvider.class.getName())) {
                throw new PersistenceException(
                    "No persistence provider specified for " + SecureEntityManagerFactory.class.getName() + ". "
                        + "Specify its class name via property \"" + NATIVE_PERSISTENCE_PROVIDER_PROPERTY + "\"");
            }
            Class<?> persistenceProviderClass
                = getClassLoader(persistenceUnitInfo).loadClass(nativePersistenceProviderClassName);
            properties.put(PERSISTENCE_PROVIDER_PROPERTY, nativePersistenceProviderClassName);
            return (PersistenceProvider)persistenceProviderClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new PersistenceException(e);
        }
    }

    private PersistenceProvider createNativePersistenceProvider(String unitName, Map properties) {
        try {
            Enumeration<URL> persistenceXmls
                = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
            XmlParser parser = new XmlParser(Collections.list(persistenceXmls));
            return createNativePersistenceProvider(unitName, properties, parser);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not initialize xml parser", e);
            return null;
        }
    }

    private PersistenceProvider createNativePersistenceProvider(String unitName, Map properties, XmlParser xmlParser) {
        String overriddenPersistenceProviderClassName = (String)properties.get(PERSISTENCE_PROVIDER_PROPERTY);
        if (isOtherPersistenceProvider(overriddenPersistenceProviderClassName)) {
            return null;
        }
        String persistenceProviderClassName = getPersistenceProviderClassName(unitName, xmlParser);
        if (isOtherPersistenceProvider(overriddenPersistenceProviderClassName, persistenceProviderClassName)) {
            return null;
        }
        String nativePersistenceProviderClassName
            = getNativePersistenceProviderClassName(unitName,
                                                    overriddenPersistenceProviderClassName,
                                                    persistenceProviderClassName,
                                                    properties,
                                                    xmlParser);
        properties.put(PERSISTENCE_PROVIDER_PROPERTY, nativePersistenceProviderClassName);
        return createNativePersistenceProvider(nativePersistenceProviderClassName);
    }

    private PersistenceProvider createNativePersistenceProvider(String persistenceProviderClassName) {
        try {
            if (persistenceProviderClassName == null
                || persistenceProviderClassName.equals(SecurePersistenceProvider.class.getName())) {
                throw new PersistenceException(
                    "No persistence provider specified for " + SecureEntityManagerFactory.class.getName() + ". "
                        + "Specify its class name via property \"" + NATIVE_PERSISTENCE_PROVIDER_PROPERTY + "\"");
            }
            Class<?> persistenceProviderClass
                = Thread.currentThread().getContextClassLoader().loadClass(persistenceProviderClassName);
            return (PersistenceProvider)persistenceProviderClass.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new PersistenceException(e);
        }
    }

    private ClassLoader getClassLoader(PersistenceUnitInfo persistenceUnitInfo) {
        if (persistenceUnitInfo.getClassLoader() != null) {
            return persistenceUnitInfo.getClassLoader();
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
