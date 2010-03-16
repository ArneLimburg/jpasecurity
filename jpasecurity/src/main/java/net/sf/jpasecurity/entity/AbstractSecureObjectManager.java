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
package net.sf.jpasecurity.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.cglib.proxy.Callback;
import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectManager implements SecureObjectManager {

    private MappingInformation mappingInformation;
    private AccessManager accessManager;

    public AbstractSecureObjectManager(MappingInformation mappingInformation,
            AccessManager accessManager) {
        this.mappingInformation = mappingInformation;
        this.accessManager = accessManager;
    }

    public boolean isSecureObject(Object object) {
        return object instanceof SecureObject;
    }

    public <T> T getSecureObject(T object) {
        if (object == null) {
            return null;
        }
        if (object instanceof List) {
            return (T)new SecureList((List)object, this, accessManager);
        } else if (object instanceof SortedSet) {
            return (T)new SecureSortedSet((SortedSet)object, this, accessManager);
        } else if (object instanceof Set) {
            return (T)new SecureSet((Set)object, this, accessManager);
        } else if (object instanceof Collection) {
            return (T)new DefaultSecureCollection((Collection)object, this, accessManager);
        } else {
            EntityInvocationHandler entityInvocationHandler
                = new EntityInvocationHandler(mappingInformation, accessManager, this, object);
            return (T)entityInvocationHandler.createSecureEntity();
        }
    }

    <T> T getUnsecureObject(T secureObject) {
        if (secureObject == null) {
            return null;
        }
        if (secureObject instanceof EntityProxy) {
            secureObject = (T)((EntityProxy)secureObject).getEntity();
        }
        if (secureObject instanceof SecureEntity) {
            try {
                for (Callback callback: (Callback[])ReflectionUtils.invokeMethod(secureObject, "getCallbacks")) {
                    if (callback instanceof EntityInvocationHandler) {
                        return (T)((EntityInvocationHandler)callback).entity;
                    }
                }
            } catch (SecurityException e) {
                // ignore
            }
            return secureObject;
        } else if (secureObject instanceof AbstractSecureCollection) {
            return (T)((AbstractSecureCollection)secureObject).getOriginal();
        } else if (secureObject instanceof SecureList) {
            return (T)((SecureList)secureObject).getOriginal();
        } else {
            return createUnsecureObject(secureObject);
        }
    }

    abstract <T> T createUnsecureObject(T secureObject);

    void secureCopy(Object unsecureObject, Object secureObject) {
        ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(unsecureObject);
            if (propertyMapping.isRelationshipMapping()) {
                value = getSecureObject(value);
            }
            propertyMapping.setPropertyValue(secureObject, value);
        }
    }

    void unsecureCopy(AccessType accessType, Object secureObject, Object unsecureObject) {
        ClassMappingInformation classMapping = getClassMapping(unsecureObject.getClass());
        boolean accessChecked = false;
        for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
            if (propertyMapping.isIdProperty()) {
                continue; //don't change id property
            }
            Object secureValue = propertyMapping.getPropertyValue(secureObject);
            if (secureValue instanceof SecureCollection) {
                if (((SecureCollection<?>)secureValue).isDirty()) {
                    if (!accessChecked) {
                        checkAccess(accessType, secureObject);
                        accessChecked = true;
                    }
                    if (accessType == AccessType.UPDATE) {
                        ((SecureCollection<?>)secureValue).flush();
                    }
                }
                continue;
            }
            Object newValue = secureValue;
            if (secureValue instanceof Collection) {
                Collection<Object> unsecureCollection = createCollection(secureValue);
                for (Object entry: (Collection<?>)secureValue) {
                    unsecureCollection.add(getUnsecureObject(entry));
                }
                newValue = unsecureCollection;
            } else if (propertyMapping.isRelationshipMapping()) {
                newValue = getUnsecureObject(secureValue);
            }
            Object oldValue = propertyMapping.getPropertyValue(unsecureObject);
            if (isDirty(propertyMapping, newValue, oldValue)) {
                if (!accessChecked) {
                    checkAccess(accessType, secureObject);
                    accessChecked = true;
                }
                propertyMapping.setPropertyValue(unsecureObject, newValue);
            }
        }
    }

    boolean isDirty(PropertyMappingInformation propertyMapping, Object newValue, Object oldValue) {
        return propertyMapping.isRelationshipMapping()? newValue != oldValue: !nullSaveEquals(newValue, oldValue);
    }

    ClassMappingInformation getClassMapping(Class<?> type) {
        return mappingInformation.getClassMapping(type);
    }

    void checkAccess(AccessType accessType, Object entity) {
        if (!accessManager.isAccessible(accessType, entity)) {
            throw new SecurityException("The current user is not permitted to " + accessType.toString().toLowerCase() + " the specified object of type " + getClassMapping(entity.getClass()).getEntityType().getName());
        }
    }

    private Collection<Object> createCollection(Object original) {
        if (original instanceof SortedSet) {
            return new TreeSet<Object>(((SortedSet<Object>)original).comparator());
        } else if (original instanceof Set) {
            return new LinkedHashSet<Object>();
        } else {
            return new ArrayList<Object>();
        }
    }

    private boolean nullSaveEquals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null) {
            return false;
        }
        return object1.equals(object2);
    }
}
