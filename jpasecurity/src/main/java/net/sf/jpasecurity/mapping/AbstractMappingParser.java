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
package net.sf.jpasecurity.mapping;

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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;


/**
 * Parses persistence units and created mapping information.
 * <strong>This class is not thread-safe</strong>
 * @author Arne Limburg
 */
public abstract class AbstractMappingParser {

    private static final String CLASS_ENTRY_SUFFIX = ".class";
    private static final String IS_PROPERTY_PREFIX = "is";
    private static final String GET_PROPERTY_PREFIX = "get";
    private static final String SET_PROPERTY_PREFIX = "set";

    private Map<Class<?>, ClassMappingInformation> classMappings;
    private Map<String, String> namedQueries;
    private ClassLoader classLoader;

    /**
     * Parses the specified persistence unit information and returns mapping information.
     */
    public MappingInformation parse(PersistenceUnitInfo persistenceUnitInfo) {
        return parse(persistenceUnitInfo, null);
    }

    /**
     * Parses the specified persistence unit information and returns mapping information,
     * merging the specified mapping information.
     * @param persistenceUnitInfo the persistence unit information
     * @param mappingInformation the mapping information to merge, may be <tt>null</tt>
     */
    public MappingInformation parse(PersistenceUnitInfo persistenceUnitInfo, MappingInformation mappingInformation) {
        classMappings = new HashMap<Class<?>, ClassMappingInformation>();
        namedQueries = new HashMap<String, String>();
        classLoader = findClassLoader(persistenceUnitInfo);
        if (mappingInformation != null) {
            for (Class<?> type: mappingInformation.getPersistentClasses()) {
                classMappings.put(type, mappingInformation.getClassMapping(type));
            }
            for (String name: mappingInformation.getNamedQueryNames()) {
                namedQueries.put(name, mappingInformation.getNamedQuery(name));
            }
        }
        parsePersistenceUnit(persistenceUnitInfo);
        return new MappingInformation(persistenceUnitInfo.getPersistenceUnitName(), classMappings, namedQueries);
    }

    protected void parse(URL url) {
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
            throw new PersistenceException(e);
        }
    }

    private String convertFileToClassname(String name) {
        return name.substring(0, name.length() - CLASS_ENTRY_SUFFIX.length()).replace('/', '.');
    }

    protected ClassMappingInformation parse(Class<?> mappedClass) {
        return parse(mappedClass, false);
    }

    protected ClassMappingInformation parse(Class<?> mappedClass, boolean override) {
        ClassMappingInformation classMapping = classMappings.get(mappedClass);
        if (classMapping != null && !override) {
            return classMapping;
        }
        Class<?> superclass = mappedClass.getSuperclass();
        ClassMappingInformation superclassMapping = null;
        if (superclass != null) {
            superclassMapping = parse(mappedClass.getSuperclass());
        }
        if (!isMapped(mappedClass)) {
            return superclassMapping;
        }
        parseNamedQueries(mappedClass);
        boolean usesFieldAccess;
        if (superclassMapping != null) {
            usesFieldAccess = superclassMapping.usesFieldAccess();
        } else {
            usesFieldAccess = usesFieldAccess(mappedClass);
        }
        Class<?> idClass = null;
        if (superclassMapping == null || superclassMapping.getIdClass() == null) {
            idClass = getIdClass(mappedClass, usesFieldAccess);
        }
        String entityName = getEntityName(mappedClass);
        boolean metadataComplete = isMetadataComplete(mappedClass);
        if (classMapping == null) {
            classMapping = new ClassMappingInformation(entityName,
                                                       mappedClass,
                                                       superclassMapping,
                                                       idClass,
                                                       usesFieldAccess,
                                                       metadataComplete);
            classMappings.put(mappedClass, classMapping);
        } else {
            classMapping.setEntityName(entityName);
            classMapping.setIdClass(idClass);
            classMapping.setFieldAccess(usesFieldAccess);
            classMapping.setMetadataComplete(metadataComplete);
        }
        if (metadataComplete) {
            classMapping.clearPropertyMappings();
        }
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
            throw new PersistenceException(e);
        }
    }

    protected Enumeration<URL> getResources(String name) {
        try {
            return classLoader.getResources(name);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private void parse(ClassMappingInformation classMapping, Member property) {
        String name = getName(property);
        Class<?> type = getType(property);
        boolean isIdProperty = isIdProperty(property);
        boolean isVersionProperty = isVersionProperty(property);
        boolean isSingleValuedRelationshipProperty = isSingleValuedRelationshipProperty(property);
        boolean isCollectionValuedRelationshipProperty = isCollectionValuedRelationshipProperty(property);
        PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(name);
        if (propertyMapping != null) {
            if (isIdProperty) {
                propertyMapping.setIdProperty(isIdProperty);
            }
            if (isVersionProperty) {
                propertyMapping.setVersionProperty(isVersionProperty);
            }
        }
        if (isSingleValuedRelationshipProperty || isCollectionValuedRelationshipProperty) {
            if (propertyMapping != null) {
                RelationshipMappingInformation relationshipMapping = (RelationshipMappingInformation)propertyMapping;
                if (isFetchTypePresent(property)) {
                    relationshipMapping.setFetchType(getFetchType(property));
                }
                CascadeType[] cascadeTypes = getCascadeTypes(property);
                if (cascadeTypes.length > 0) {
                    relationshipMapping.setCascadeTypes(getCascadeTypes(property));
                }
            } else {
                if (isSingleValuedRelationshipProperty) {
                    ClassMappingInformation typeMapping = parse(type);
                    propertyMapping = new SingleValuedRelationshipMappingInformation(name,
                                                                                     typeMapping,
                                                                                     classMapping,
                                                                                     isIdProperty,
                                                                                     getFetchType(property),
                                                                                     getCascadeTypes(property));
                } else if (isCollectionValuedRelationshipProperty) {
                    ClassMappingInformation targetMapping = parse(getTargetType(property));
                    propertyMapping = new CollectionValuedRelationshipMappingInformation(name,
                                                                                         type,
                                                                                         targetMapping,
                                                                                         classMapping,
                                                                                         isIdProperty,
                                                                                         getFetchType(property),
                                                                                         getCascadeTypes(property));
                }
                classMapping.addPropertyMapping(propertyMapping);
            }
        } else if (propertyMapping == null && isSimplePropertyType(type)) {
            propertyMapping
                = new SimplePropertyMappingInformation(name, type, classMapping, isIdProperty, isVersionProperty);
            classMapping.addPropertyMapping(propertyMapping);
        } else if (propertyMapping == null) {
            throw new PersistenceException("could not determine mapping for property \"" + name + "\" of class " + property.getDeclaringClass().getName());
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
            throw new PersistenceException("no target entity specified for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
        Type[] genericTypeArguments = ((ParameterizedType)genericType).getActualTypeArguments();
        Type genericTypeArgument;
        if (genericTypeArguments.length == 1) {
            genericTypeArgument = genericTypeArguments[0];
        } else if (genericTypeArguments.length == 2) {
            //Must be a map, take the value
            genericTypeArgument = genericTypeArguments[1];
        } else {
            throw new PersistenceException("could not determine target entity for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
        if (genericTypeArgument instanceof Class) {
            return (Class<?>)genericTypeArgument;
        } else {
            Type[] bounds = null;
            if (genericTypeArgument instanceof TypeVariable) {
                bounds = ((TypeVariable)genericTypeArgument).getBounds();
            } else if (genericTypeArgument instanceof WildcardType) {
                bounds = ((WildcardType)genericTypeArgument).getUpperBounds();
            }
            if (bounds != null) {
                for (Type bound: ((TypeVariable)genericTypeArgument).getBounds()) {
                    if (bound instanceof Class) {
                        return (Class<?>)bound;
                    }
                }
            }
            throw new PersistenceException("could not determine target entity for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
    }

    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Field[] fields = mappedClass.getDeclaredFields();
        for (Field field: fields) {
            if (isMappable(field) && isMapped(field)) {
                return true;
            }
        }
        return false;
    }

    protected abstract void parsePersistenceUnit(PersistenceUnitInfo persistenceUnitInfo);

    protected void parseNamedQueries(Class<?> mappedClass) {
    }

    protected void addNamedQuery(String name, String query) {
        namedQueries.put(name, query);
    }

    protected abstract boolean isMapped(Class<?> mappedClass);

    protected abstract boolean isMapped(Member member);

    protected abstract boolean isMetadataComplete(Class<?> entityClass);

    protected abstract Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess);

    protected boolean isMappable(Member member) {
        return !Modifier.isStatic(member.getModifiers()) && !Modifier.isTransient(member.getModifiers());
    }

    protected boolean isSimplePropertyType(Class<?> type) {
        return isEmbeddable(type)
            || type.isPrimitive()
            || type.equals(Boolean.class)
            || type.equals(Byte.class)
            || type.equals(Short.class)
            || type.equals(Integer.class)
            || type.equals(Long.class)
            || type.equals(BigInteger.class)
            || type.equals(Float.class)
            || type.equals(Double.class)
            || type.equals(BigDecimal.class)
            || type.equals(Character.class)
            || type.equals(String.class)
            || type.equals(java.util.Date.class)
            || type.equals(Calendar.class)
            || type.equals(java.sql.Date.class)
            || type.equals(Time.class)
            || type.equals(Timestamp.class)
            || type.equals(byte[].class)
            || type.equals(Byte[].class)
            || type.equals(char[].class)
            || type.equals(Character[].class)
            || Enum.class.isAssignableFrom(type)
            || Serializable.class.isAssignableFrom(type);
    }

    protected abstract boolean isEmbeddable(Class<?> type);

    protected abstract boolean isIdProperty(Member property);

    protected abstract boolean isVersionProperty(Member property);

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

    private ClassLoader findClassLoader(PersistenceUnitInfo persistenceUnit) {
        ClassLoader classLoader = persistenceUnit.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
