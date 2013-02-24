/*
 * Copyright 2012 Arne Limburg
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
package net.sf.jpasecurity.security;

import static net.sf.jpasecurity.util.Validate.notNull;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.entity.AbstractSecureObjectManager;
import net.sf.jpasecurity.entity.SecureEntityDecorator;
import net.sf.jpasecurity.entity.SecureEntityInterceptor;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.proxy.Decorator;
import net.sf.jpasecurity.proxy.EntityProxy;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.util.DoubleKeyHashMap;
import net.sf.jpasecurity.util.ReflectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arne Limburg
 */
public class DefaultAccessManager implements AccessManager {

    private static final Log LOG = LogFactory.getLog(DefaultAccessManager.class);

    private MappingInformation mappingInformation;
    private BeanInitializer beanInitializer;
    private SecureEntityProxyFactory proxyFactory;
    private AbstractSecureObjectManager objectManager;
    private EntityFilter entityFilter;
    private DoubleKeyHashMap<ClassMappingInformation, Object, Boolean> cachedReadAccess
        = new DoubleKeyHashMap<ClassMappingInformation, Object, Boolean>();

    public DefaultAccessManager(MappingInformation mappingInformation,
                                BeanInitializer beanInitializer,
                                SecureEntityProxyFactory secureEntityProxyFactory,
                                AbstractSecureObjectManager objectManager,
                                EntityFilter entityFilter) {
        notNull(MappingInformation.class, mappingInformation);
        notNull(BeanInitializer.class, beanInitializer);
        notNull(SecureEntityProxyFactory.class, secureEntityProxyFactory);
        notNull(AbstractSecureObjectManager.class, objectManager);
        notNull(EntityFilter.class, entityFilter);
        this.mappingInformation = mappingInformation;
        this.beanInitializer = beanInitializer;
        this.proxyFactory = secureEntityProxyFactory;
        this.objectManager = objectManager;
        this.entityFilter = entityFilter;
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(entityName);
        Object[] transientParameters = new Object[parameters.length];
        for (int i = 0; i < transientParameters.length; i++) {
            Object parameter = parameters[i];
            if (parameter instanceof EntityProxy) {
                parameter = ((EntityProxy)parameter).getEntity();
            }
            if (parameter != null && mappingInformation.containsClassMapping(parameter.getClass())) {
                ClassMappingInformation mapping = mappingInformation.getClassMapping(parameter.getClass());
                MethodInterceptor interceptor = new SecureEntityInterceptor(beanInitializer, objectManager, parameter);
                Decorator<SecureEntity> decorator = new SecureEntityDecorator(mapping, beanInitializer, this,
                                                                              objectManager, parameter, true);
                transientParameters[i] = proxyFactory.createSecureEntityProxy(mapping.getEntityType(),
                                                                              interceptor,
                                                                              decorator);
            } else {
                transientParameters[i] = parameter;
            }
        }
        Object entity = null;
        try {
            entity = ReflectionUtils.newInstance(classMapping.getEntityType(), transientParameters);
        } catch (RuntimeException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Constructor of " + classMapping.getEntityType()
                          + " threw exception, hence isAccessible returns false.", e);
            } else {
                LOG.info("Constructor of " + classMapping.getEntityType()
                         + " threw exception (\"" + e.getMessage() + "\"), hence isAccessible returns false.");
            }
            return false;
        }
        return isAccessible(accessType, entity);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        if (entity instanceof EntityProxy) {
            entity = ((EntityProxy)entity).getEntity();
        }
        if (entity == null) {
            return false;
        }
        final ClassMappingInformation classMapping = mappingInformation.getClassMapping(entity.getClass());
        final Object entityId = classMapping.getId(entity);
        if (accessType == AccessType.READ) {
            final Boolean isAccessible = cachedReadAccess.get(classMapping, entityId);
            if (isAccessible != null) {
                return isAccessible;
            }
        }
        try {
            final boolean accessible = entityFilter.isAccessible(accessType, entity);
            if (accessType == AccessType.READ) {
                cachedReadAccess.put(classMapping, entityId, accessible);
            }
            return accessible;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
