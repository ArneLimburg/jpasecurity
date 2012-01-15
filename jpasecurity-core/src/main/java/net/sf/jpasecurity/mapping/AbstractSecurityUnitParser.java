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
package net.sf.jpasecurity.mapping;

import static net.sf.jpasecurity.util.Types.isSimplePropertyType;
import static net.sf.jpasecurity.util.Validate.notNull;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.FetchType;
import net.sf.jpasecurity.SecurityUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses security units and created mapping information.
 * <strong>This class is not thread-safe</strong>
 * @author Arne Limburg
 */
public abstract class AbstractSecurityUnitParser {

    private static final Log LOG = LogFactory.getLog(AbstractSecurityUnitParser.class);

    private static final String CLASS_ENTRY_SUFFIX = ".class";
    private static final String IS_PROPERTY_PREFIX = "is";
    private static final String GET_PROPERTY_PREFIX = "get";
    private static final String SET_PROPERTY_PREFIX = "set";

    protected final PropertyAccessStrategyFactory propertyAccessStrategyFactory;
    protected final ExceptionFactory exceptionFactory;

    private SecurityUnit securityUnit;
    private Map<Class<?>, DefaultClassMappingInformation> classMappings;
    private Map<String, String> namedQueries;
    private List<EntityListener> defaultEntityListeners;
    private ClassLoader classLoader;

    public AbstractSecurityUnitParser(SecurityUnit securityUnit,
                                      PropertyAccessStrategyFactory propertyAccessStrategyFactory,
                                      ExceptionFactory exceptionFactory) {
        notNull(SecurityUnit.class, securityUnit);
        notNull(PropertyAccessStrategyFactory.class, propertyAccessStrategyFactory);
        notNull(ExceptionFactory.class, exceptionFactory);
        this.securityUnit = securityUnit;
        this.propertyAccessStrategyFactory = propertyAccessStrategyFactory;
        this.exceptionFactory = exceptionFactory;
    }

    protected SecurityUnit getSecurityUnit() {
        return securityUnit;
    }

    protected PropertyAccessStrategyFactory getPropertyAccessStrategyFactory() {
        return propertyAccessStrategyFactory;
    }

    protected ExceptionFactory getExceptionFactory() {
        return exceptionFactory;
    }

    /**
     * Parses the specified security unit information and returns mapping information,
     * merging the specified mapping information.
     * @param securityUnitInformation the security unit information
     * @param mappingInformation the mapping information to merge, may be <tt>null</tt>
     */
    public MappingInformation parse() {
        classMappings = new HashMap<Class<?>, DefaultClassMappingInformation>();
        namedQueries = new HashMap<String, String>();
        defaultEntityListeners = new ArrayList<EntityListener>();
        classLoader = findClassLoader(securityUnit);
        parseSecurityUnit(securityUnit);
        String securityUnitName = securityUnit.getSecurityUnitName();
        return new DefaultMappingInformation(securityUnitName, classMappings, namedQueries, exceptionFactory);
    }

    protected void parseSecurityUnit(SecurityUnit securityUnit) {
        if (!securityUnit.excludeUnlistedClasses()) {
            if (securityUnit.getSecurityUnitRootUrl() != null) {
                parse(securityUnit.getSecurityUnitRootUrl());
            }
        }
        for (URL url: securityUnit.getJarFileUrls()) {
            parse(url);
        }
        for (String className: securityUnit.getManagedClassNames()) {
            parse(getClass(className));
        }
    }

    protected void parse(URL url) {
        //TODO support file urls
        try {
            InputStream in = url.openStream();
            try {
                ZipInputStream zipStream = new ZipInputStream(in);
                for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry()) {
                    if (entry.getName().endsWith(CLASS_ENTRY_SUFFIX)) {
                        parse(getClass(convertFileToClassname(entry.getName())));
                    }
                    zipStream.closeEntry();
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw exceptionFactory.createRuntimeException(e);
        }
    }

    private String convertFileToClassname(String name) {
        return name.substring(0, name.length() - CLASS_ENTRY_SUFFIX.length()).replace('/', '.');
    }

    protected ClassMappingInformation parse(Class<?> mappedClass) {
        return parse(mappedClass, false, false);
    }

    protected ClassMappingInformation parse(Class<?> mappedClass, boolean derivedFieldAccess, boolean override) {
        DefaultClassMappingInformation classMapping = classMappings.get(mappedClass);
        if (classMapping != null && !override) {
            return classMapping;
        }
        Class<?> superclass = mappedClass.getSuperclass();
        ClassMappingInformation superclassMapping = null;
        if (superclass != null) {
            superclassMapping = parse(mappedClass.getSuperclass(), derivedFieldAccess, override);
        }
        if (!isMapped(mappedClass)) {
            return superclassMapping;
        }
        LOG.debug("Parsing " + mappedClass.getName());
        parseNamedQueries(mappedClass);
        boolean usesFieldAccess;
        if (isFieldAccessDerived(mappedClass)) {
            usesFieldAccess = derivedFieldAccess;
        } else if (superclassMapping != null) {
            usesFieldAccess = superclassMapping.usesFieldAccess();
        } else {
            usesFieldAccess = usesFieldAccess(mappedClass);
        }
        ClassMappingInformation idClassMapping = null;
        if (superclassMapping == null || superclassMapping.getIdClassMapping() == null) {
            Class<?> idClass = getIdClass(mappedClass, usesFieldAccess);
            if (idClass != null) {
                idClassMapping = parse(idClass, derivedFieldAccess, override);
            }
        }
        String entityName = getEntityName(mappedClass);
        boolean metadataComplete = isMetadataComplete(mappedClass);
        if (classMapping == null) {
            classMapping = new DefaultClassMappingInformation(entityName,
                                                              mappedClass,
                                                              (DefaultClassMappingInformation)superclassMapping,
                                                              idClassMapping,
                                                              isEmbeddable(mappedClass),
                                                              usesFieldAccess,
                                                              metadataComplete,
                                                              exceptionFactory);
            classMappings.put(mappedClass, classMapping);
        } else {
            classMapping.setEntityName(entityName);
            classMapping.setIdClassMapping(idClassMapping);
            classMapping.setFieldAccess(usesFieldAccess);
            classMapping.setMetadataComplete(metadataComplete);
        }
        if (metadataComplete) {
            classMapping.clearPropertyMappings();
        }
        if (!excludeDefaultEntityListeners(mappedClass)) {
            classMapping.setDefaultEntityListeners(Collections.unmodifiableList(this.defaultEntityListeners));
        }
        parseEntityListeners(classMapping);
        classMapping.setSuperclassEntityListenersExcluded(excludeSuperclassEntityListeners(mappedClass));
        parseEntityLifecycleMethods(classMapping);
        if (usesFieldAccess) {
            for (Field field: mappedClass.getDeclaredFields()) {
                if (isMappable(field)) {
                    parse(classMapping, field);
                }
            }
        } else {
            for (Method method: mappedClass.getDeclaredMethods()) {
                if (isPropertyGetter(method)) {
                    parse(classMapping, method);
                }
            }
        }
        return classMapping;
    }

    protected Class<?> getClass(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw exceptionFactory.createRuntimeException(e);
        }
    }

    protected Enumeration<URL> getResources(String name) {
        try {
            return classLoader.getResources(name);
        } catch (IOException e) {
            throw exceptionFactory.createRuntimeException(e);
        }
    }

    private void parse(DefaultClassMappingInformation classMapping, Member property) {
        String name = getName(property);
        LOG.trace("Parsing property " + classMapping.getEntityName() + "." + name);
        Class<?> type = getType(property);
        boolean isSingleValuedRelationshipProperty = isSingleValuedRelationshipProperty(property);
        boolean isCollectionValuedRelationshipProperty = isCollectionValuedRelationshipProperty(property);
        boolean createPropertyMapping = !classMapping.containsPropertyMapping(name);
        AbstractPropertyMappingInformation propertyMapping = null;
        if (!createPropertyMapping) {
            propertyMapping = classMapping.getPropertyMapping(name);
            LOG.trace("Property already parsed, is of type " + propertyMapping.getProperyType().getSimpleName());
        }
        if (isSingleValuedRelationshipProperty || isCollectionValuedRelationshipProperty) {
            LOG.trace("Property is relationship property");
            if (propertyMapping != null) {
                LOG.trace("Property already parsed, adding fetch- and cascade-information");
                RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
                if (isFetchTypePresent(property)) {
                    relationshipMapping.setFetchType(getFetchType(property));
                }
                CascadeType[] cascadeTypes = getCascadeTypes(property);
                if (cascadeTypes.length > 0) {
                    relationshipMapping.setCascadeTypes(getCascadeTypes(property));
                }
            } else {
                LOG.trace("Property not parsed, creating...");
                if (isSingleValuedRelationshipProperty) {
                    ClassMappingInformation typeMapping = parse(type, classMapping.usesFieldAccess(), false);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Property " + classMapping.getEntityName() + "." + name
                                  + " is single-valued relationship of type " + typeMapping.getEntityName());
                    }
                    PropertyAccessStrategy propertyAccessStrategy
                        = propertyAccessStrategyFactory.createPropertyAccessStrategy(classMapping, name);
                    propertyMapping = new SingleValuedRelationshipMappingInformation(name,
                                                                                     typeMapping,
                                                                                     classMapping,
                                                                                     propertyAccessStrategy,
                                                                                     exceptionFactory,
                                                                                     getFetchType(property),
                                                                                     getCascadeTypes(property));
                } else if (isCollectionValuedRelationshipProperty) {
                    ClassMappingInformation targetMapping
                        = parse(getTargetType(property), classMapping.usesFieldAccess(), false);
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Property " + classMapping.getEntityName() + "." + name
                                  + " is single-valued relationship of type "
                                  + type.getSimpleName() + "<" + targetMapping.getEntityName() + ">");
                    }
                    PropertyAccessStrategy propertyAccessStrategy
                        = propertyAccessStrategyFactory.createPropertyAccessStrategy(classMapping, name);
                    propertyMapping = new CollectionValuedRelationshipMappingInformation(name,
                                                                                         type,
                                                                                         targetMapping,
                                                                                         classMapping,
                                                                                         propertyAccessStrategy,
                                                                                         exceptionFactory,
                                                                                         getFetchType(property),
                                                                                         getCascadeTypes(property));
                }
                classMapping.addPropertyMapping(propertyMapping);
            }
        } else if (propertyMapping == null && (isSimplePropertyType(type) || type instanceof Serializable)) {
            LOG.trace("Property is simple property of type " + type.getSimpleName());
            PropertyAccessStrategy propertyAccessStrategy
                = propertyAccessStrategyFactory.createPropertyAccessStrategy(classMapping, name);
            propertyMapping = new SimplePropertyMappingInformation(name,
                                                                   type,
                                                                   classMapping,
                                                                   propertyAccessStrategy,
                                                                   exceptionFactory);
            classMapping.addPropertyMapping(propertyMapping);
        } else if (propertyMapping == null) {
            String error = "Could not determine mapping for property \"" + name
                         + "\" of class " + property.getDeclaringClass().getName();
            LOG.error(error);
            throw exceptionFactory.createMappingException(error);
        }
        if (isIdProperty(property)) {
            LOG.trace("Property " + name + " is id-property");
            propertyMapping.setIdProperty(true);
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("Property " + name + " is no id-property");
        }
        if (isVersionProperty(property)) {
            LOG.trace("Property " + name + " is version-property");
            propertyMapping.setVersionProperty(true);
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("Property " + name + " is no version-property");
        }
        if (isGeneratedValue(property)) {
            LOG.trace("Property " + name + " is generated");
            propertyMapping.setGeneratedValue(true);
        } else if (LOG.isTraceEnabled()) {
            LOG.trace("Property " + name + " is not generated");
        }
    }

    protected ClassMappingInformation getMapping(Class<?> type) {
        return classMappings.get(type);
    }

    protected String getEntityName(Class<?> entityClass) {
        return entityClass.getSimpleName();
    }

    protected String getName(Member property) {
        if (property instanceof Field) {
            return property.getName();
        }
        String name = property.getName();
        if (name.startsWith(GET_PROPERTY_PREFIX)) {
            return Introspector.decapitalize(name.substring(GET_PROPERTY_PREFIX.length()));
        } else if (property.getName().startsWith(IS_PROPERTY_PREFIX)) {
            return Introspector.decapitalize(name.substring(IS_PROPERTY_PREFIX.length()));
        } else {
            throw new IllegalArgumentException("Illegal method name for property-read-method, must start either with 'get' or 'is'");
        }
    }

    protected Class<?> getType(Member property) {
        if (property instanceof Method) {
            return ((Method)property).getReturnType();
        } else {
            return ((Field)property).getType();
        }
    }

    protected Class<?> getTargetType(Member property) {
        Type genericType;
        if (property instanceof Method) {
            genericType = ((Method)property).getGenericReturnType();
        } else {
            genericType = ((Field)property).getGenericType();
        }
        if (!(genericType instanceof ParameterizedType)) {
            throw exceptionFactory.createTargetEntityNotFoundException(property);
        }
        Type[] genericTypeArguments = ((ParameterizedType)genericType).getActualTypeArguments();
        Type genericTypeArgument;
        if (genericTypeArguments.length == 1) {
            genericTypeArgument = genericTypeArguments[0];
        } else if (genericTypeArguments.length == 2) {
            //Must be a map, take the value
            genericTypeArgument = genericTypeArguments[1];
        } else {
            throw exceptionFactory.createTargetEntityNotFoundException(property);
        }
        if (genericTypeArgument instanceof Class) {
            return (Class<?>)genericTypeArgument;
        } else {
            Type[] bounds = null;
            if (genericTypeArgument instanceof TypeVariable) {
                bounds = ((TypeVariable<?>)genericTypeArgument).getBounds();
            } else if (genericTypeArgument instanceof WildcardType) {
                bounds = ((WildcardType)genericTypeArgument).getUpperBounds();
            }
            if (bounds != null) {
                for (Type bound: ((TypeVariable<?>)genericTypeArgument).getBounds()) {
                    if (bound instanceof Class) {
                        return (Class<?>)bound;
                    }
                }
            }
            throw exceptionFactory.createTargetEntityNotFoundException(property);
        }
    }

    protected boolean isFieldAccessDerived(Class<?> mappedClass) {
        return isEmbeddable(mappedClass);
    }

    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Field[] fields = mappedClass.getDeclaredFields();
        if (LOG.isTraceEnabled()) {
            LOG.trace("parsing " + fields.length + " fields to determine access type");
        }
        for (Field field: fields) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("checking " + field.getName() + " to determine access type");
            }
            if (isIdProperty(field)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace(mappedClass.getSimpleName() + " uses field access");
                }
                return true;
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(mappedClass.getSimpleName() + " uses property access");
        }
        return false;
    }

    protected void parseNamedQueries(Class<?> mappedClass) {
    }

    protected void addNamedQuery(String name, String query) {
        namedQueries.put(name, query);
    }

    protected void addDefaultEntityListener(EntityListener entityListener) {
        defaultEntityListeners.add(entityListener);
    }

    protected abstract boolean isMapped(Class<?> mappedClass);

    protected abstract boolean isMapped(Member member);

    protected abstract boolean isMetadataComplete(Class<?> entityClass);

    protected abstract boolean excludeDefaultEntityListeners(Class<?> entityClass);

    protected abstract boolean excludeSuperclassEntityListeners(Class<?> entityClass);

    protected abstract void parseEntityListeners(DefaultClassMappingInformation classMapping);

    protected abstract void parseEntityLifecycleMethods(DefaultClassMappingInformation classMapping);

    protected void addEntityListener(DefaultClassMappingInformation classMappingInformation,
                                     Class<?> type,
                                     EntityListener entityListener) {
        classMappingInformation.addEntityListener(type, entityListener);
    }

    protected void setEntityLifecycleMethods(DefaultClassMappingInformation classMappingInformation,
                                             EntityLifecycleMethods entityLifecycleMethods) {
        classMappingInformation.setEntityLifecycleMethods(entityLifecycleMethods);
    }

    protected abstract Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess);

    protected boolean isMappable(Member member) {
        return !Modifier.isStatic(member.getModifiers()) && !Modifier.isTransient(member.getModifiers());
    }

    protected abstract boolean isEmbeddable(Class<?> type);

    protected abstract boolean isIdProperty(Member property);

    protected abstract boolean isVersionProperty(Member property);

    protected abstract boolean isGeneratedValue(Member property);

    protected abstract boolean isFetchTypePresent(Member property);

    protected FetchType getFetchType(Member property) {
        Class<?> type = getType(property);
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return FetchType.LAZY;
        }
        return FetchType.EAGER;
    }

    protected abstract CascadeType[] getCascadeTypes(Member property);

    protected boolean isRelationshipProperty(Member property) {
        return isSingleValuedRelationshipProperty(property) || isCollectionValuedRelationshipProperty(property);
    }

    protected abstract boolean isSingleValuedRelationshipProperty(Member property);

    protected abstract boolean isCollectionValuedRelationshipProperty(Member property);

    private boolean isPropertyGetter(Method method) {
        if ((!method.getName().startsWith(GET_PROPERTY_PREFIX)
             && !method.getName().startsWith(IS_PROPERTY_PREFIX))
            || method.getParameterTypes().length != 0
            || method.getReturnType() == void.class
            || !isMappable(method)) {
            return false;
        }
        String propertyName = getName(method);
        String propertySetterName
            = SET_PROPERTY_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Class<?> entityClass = method.getDeclaringClass();
        Class<?> propertyType = method.getReturnType();
        return hasPropertySetter(entityClass, propertySetterName, propertyType);
    }

    private boolean hasPropertySetter(Class<?> entityClass, String propertySetterName, Class<?> propertyType) {
        if (entityClass == null) {
            return false;
        }
        for (Method method: entityClass.getDeclaredMethods()) {
            if (method.getName().equals(propertySetterName)
                && method.getParameterTypes().length == 1
                && method.getReturnType() == void.class) {
                return method.getParameterTypes()[0].isAssignableFrom(propertyType);
            }
        }
        return hasPropertySetter(entityClass.getSuperclass(), propertySetterName, propertyType);
    }

    private ClassLoader findClassLoader(SecurityUnit securityUnitInformation) {
        ClassLoader classLoader = securityUnitInformation.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
