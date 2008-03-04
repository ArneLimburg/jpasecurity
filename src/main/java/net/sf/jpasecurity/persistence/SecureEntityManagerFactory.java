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
package net.sf.jpasecurity.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesProvider;

/**
 * @author Arne Limburg
 */
public class SecureEntityManagerFactory implements EntityManagerFactory {

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "net.sf.jpasecurity.persistence.provider";
    public static final String AUTHENTICATION_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.authentication.provider";
    public static final String ACCESS_RULES_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.rules.provider";
    public static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS = "net.sf.jpasecurity.security.rules.XmlAccessRulesProvider";

    private EntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private AuthenticationProvider authenticationProvider;
    private AccessRulesProvider accessRulesProvider;

    public SecureEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
    		                          Map<String, String> properties,
    		                          boolean isContainerManaged) {
    	this(persistenceUnitInfo,
    	     properties,
    	     createEntityManagerFactory(properties, persistenceUnitInfo, isContainerManaged),
    	     createAuthenticationProvider(properties, persistenceUnitInfo),
    	     createAccessRulesProvider(properties, persistenceUnitInfo),
    	     isContainerManaged);
    }
    
    public SecureEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
                                      Map<String, String> properties,
                                      EntityManagerFactory entityManagerFactory,
                                      boolean isContainerManaged) {
    	this(persistenceUnitInfo,
       	     properties,
       	     entityManagerFactory,
       	     createAuthenticationProvider(properties, persistenceUnitInfo),
       	     createAccessRulesProvider(properties, persistenceUnitInfo),
       	     isContainerManaged);
	}
    
    SecureEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
                               Map<String, String> properties,
                               EntityManagerFactory entityManagerFactory,
                               AuthenticationProvider authenticationProvider,
                               AccessRulesProvider accessRulesProvider,
                               boolean isContainerManaged) {
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("entityManagerFactory may not be null");
        }
        if (authenticationProvider == null) {
            throw new IllegalArgumentException("authenticationProvider may not be null");
        }
        if (accessRulesProvider == null) {
            throw new IllegalArgumentException("accessRulesProvider may not be null");
        }
        if (persistenceUnitInfo == null) {
            throw new IllegalArgumentException("persistenceUnitInfo may not be null");
        }
    	this.entityManagerFactory = entityManagerFactory;
    	this.authenticationProvider = authenticationProvider;
    	this.accessRulesProvider = accessRulesProvider;
    	this.mappingInformation = new MappingInformation(persistenceUnitInfo);
        Map<String, String> persistenceProperties = new HashMap<String, String>((Map)persistenceUnitInfo.getProperties());
        if (properties != null) {
            persistenceProperties.putAll(properties);
        }
        injectPersistenceInformation(persistenceProperties);
    }
    
    private void injectPersistenceInformation(Map<String, String> persistenceProperties) {
    	persistenceProperties = Collections.unmodifiableMap(persistenceProperties);
    	if (authenticationProvider instanceof PersistenceInformationReceiver) {
    		PersistenceInformationReceiver persistenceInformationReceiver
    			= (PersistenceInformationReceiver)authenticationProvider;
    		persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
    		persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
    	}
    	if (accessRulesProvider instanceof PersistenceInformationReceiver) {
    		PersistenceInformationReceiver persistenceInformationReceiver
    			= (PersistenceInformationReceiver)accessRulesProvider;
    		persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
    		persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
    	}
    }
    
    public EntityManager createEntityManager() {
        return new SecureEntityManager(entityManagerFactory.createEntityManager(),
        		                       mappingInformation,
        		                       authenticationProvider,
        		                       accessRulesProvider.getAccessRules());
    }

    public EntityManager createEntityManager(Map map) {
        return new SecureEntityManager(entityManagerFactory.createEntityManager(map),
        		                       mappingInformation,
        		                       authenticationProvider,
        		                       accessRulesProvider.getAccessRules());
    }

    public boolean isOpen() {
        return entityManagerFactory.isOpen();
    }

    public void close() {
        entityManagerFactory.close();
        entityManagerFactory = null;
        mappingInformation = null;
        authenticationProvider = null;
        accessRulesProvider = null;
    }

    private static EntityManagerFactory createEntityManagerFactory(Map<String, String> properties, PersistenceUnitInfo persistenceUnitInfo, boolean isContainerManaged) {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        PersistenceProvider persistenceProvider = createPersistenceProvider(properties, persistenceUnitInfo);
        
    	if (isContainerManaged) {
    		return persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
    	} else {
    		String name = persistenceUnitInfo.getPersistenceUnitName();
    		return persistenceProvider.createEntityManagerFactory(name, properties);
    	}    	
    }

    /**
     * As a side-effect this method sets the "javax.persistence.provider" property of the specified properties
     * to the class name of the returned persistence provider.
     * @param properties may not be <tt>null</tt>
     * @param persistenceUnitInfo
     * @return the persistence provider
     */
    private static PersistenceProvider createPersistenceProvider(Map<String, String> properties, PersistenceUnitInfo persistenceUnitInfo) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null");
        }
        try {
        	String persistenceProviderClassName = properties.get(PERSISTENCE_PROVIDER_PROPERTY);
        	if (persistenceProviderClassName == null && persistenceUnitInfo.getProperties() != null) {
        		persistenceProviderClassName
        		    = persistenceUnitInfo.getProperties().getProperty(PERSISTENCE_PROVIDER_PROPERTY);
        	}
            if (persistenceProviderClassName == null) {
                persistenceProviderClassName = persistenceUnitInfo.getPersistenceProviderClassName();
            }
        	if (persistenceProviderClassName == null) {
        		throw new PersistenceException("No persistence provider specified for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. Specify its class name via property \"" + PERSISTENCE_PROVIDER_PROPERTY + "\"");
        	}
        	properties.put(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY, persistenceProviderClassName);
            Class<?> persistenceProviderClass = getClassLoader(persistenceUnitInfo).loadClass(persistenceProviderClassName);
			return (PersistenceProvider)persistenceProviderClass.newInstance();
		} catch (InstantiationException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e);
		}
    }

    private static AuthenticationProvider createAuthenticationProvider(Map<String, String> properties, PersistenceUnitInfo persistenceUnitInfo) {
        try {
        	String authenticationProviderClassName = null;
            if (properties != null) {
                authenticationProviderClassName = properties.get(AUTHENTICATION_PROVIDER_PROPERTY);
            }
        	if (authenticationProviderClassName == null) {
        		authenticationProviderClassName
        		    = persistenceUnitInfo.getProperties().getProperty(AUTHENTICATION_PROVIDER_PROPERTY);
        	}
        	if (authenticationProviderClassName == null) {
        		throw new PersistenceException("No authentication provider specified for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. Specify its class name via property \"" + AUTHENTICATION_PROVIDER_PROPERTY + "\"");
        	}
        	Class<?> authenticationProviderClass = getClassLoader(persistenceUnitInfo).loadClass(authenticationProviderClassName);
			return (AuthenticationProvider)authenticationProviderClass.newInstance();
		} catch (InstantiationException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e);
		}
    }

    private static AccessRulesProvider createAccessRulesProvider(Map<String, String> properties, PersistenceUnitInfo persistenceUnitInfo) {
    	try {
    		String accessRulesProviderClassName = null;
            if (properties != null) {
                accessRulesProviderClassName = properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
            }
    		if (accessRulesProviderClassName == null) {
    			accessRulesProviderClassName
    			= persistenceUnitInfo.getProperties().getProperty(ACCESS_RULES_PROVIDER_PROPERTY, DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
    		}
        	Class<?> accessRulesProviderClass = getClassLoader(persistenceUnitInfo).loadClass(accessRulesProviderClassName);
			return (AccessRulesProvider)accessRulesProviderClass.newInstance();
		} catch (InstantiationException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e);
		}
    }
    
    private static ClassLoader getClassLoader(PersistenceUnitInfo persistenceUnitInfo) {
    	if (persistenceUnitInfo.getClassLoader() != null) {
    		return persistenceUnitInfo.getClassLoader();
    	}
    	return Thread.currentThread().getContextClassLoader();
    }
}
