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
package net.sf.jpasecurity.mapping.parser;

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

import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;
import net.sf.jpasecurity.mapping.SingleValuedRelationshipMappingInformation;

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
                        parse(getClass(entry.getName()));
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

    protected ClassMappingInformation parse(Class<?> mappedClass) {
        ClassMappingInformation classMapping = classMappings.get(mappedClass);
        if (classMapping == null) {
            Class<?> superclass = mappedClass.getSuperclass();
            ClassMappingInformation superclassMapping = null;
            if (superclass != null) {
                superclassMapping = parse(mappedClass.getSuperclass());
            }
            if (isMapped(mappedClass)) {
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
                classMapping = new ClassMappingInformation(entityName,
                                                           mappedClass,
                                                           superclassMapping,
                                                           idClass,
                                                           usesFieldAccess);
                classMappings.put(mappedClass, classMapping);
                if (usesFieldAccess) {
                    for (Field field: mappedClass.getDeclaredFields()) {
                        if (isMappable(field)) {
                            PropertyMappingInformation propertyMapping = parse(field);
                            classMapping.addPropertyMapping(propertyMapping);
                        }
                    }
                } else {
                    for (Method method: mappedClass.getDeclaredMethods()) {
                        if (isPropertyGetter(method)) {
                            PropertyMappingInformation propertyMapping = parse(method);
                            classMapping.addPropertyMapping(propertyMapping);
                        }
                    }
                }
            } else {
                classMapping = superclassMapping;
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

    private PropertyMappingInformation parse(Member property) {
        String name = getName(property);
        Class<?> type = getType(property);
        ClassMappingInformation classMapping = parse(property.getDeclaringClass());
        boolean isIdProperty = isIdProperty(property);
        if (isSingleValuedRelationshipProperty(property)) {
            ClassMappingInformation typeMapping = parse(type);
            return new SingleValuedRelationshipMappingInformation(name,
                                                                  typeMapping,
                                                                  classMapping,
                                                                  isIdProperty,
                                                                  getFetchType(property),
                                                                  getCascadeTypes(property));
        } else if (isCollectionValuedRelationshipProperty(property)) {
            ClassMappingInformation targetMapping = parse(getTargetType(property));
            return new CollectionValuedRelationshipMappingInformation(name,
                                                                      type,
                                                                      targetMapping,
                                                                      classMapping,
                                                                      isIdProperty,
                                                                      getFetchType(property),
                                                                      getCascadeTypes(property));
        } else if (isSimplePropertyType(type)) {
            return new SimplePropertyMappingInformation(name, type, classMapping, isIdProperty);
        } else if (classMapping.getPropertyMapping(name) != null) {
            return classMapping.getPropertyMapping(name);
        } else {
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

   //TODO newTempClassLoader seams to create problems with different instances of one class in mapper.
    private ClassLoader findClassLoader(PersistenceUnitInfo persistenceUnit) {
        ClassLoader classLoader = persistenceUnit.getNewTempClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = persistenceUnit.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
