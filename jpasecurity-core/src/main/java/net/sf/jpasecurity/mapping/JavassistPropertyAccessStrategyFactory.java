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
package net.sf.jpasecurity.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public class JavassistPropertyAccessStrategyFactory extends DefaultPropertyAccessStrategyFactory {

    public PropertyAccessStrategy createPropertyAccessStrategy(ClassMappingInformation classMapping,
                                                               String propertyName) {
        Class<?> entityType = classMapping.getEntityType();
        String className = entityType.getName();
        boolean usesFieldAccess = classMapping.usesFieldAccess();
        Member readMember;
        Member writeMember;
        if (usesFieldAccess) {
            readMember = getField(entityType, propertyName);
            writeMember = readMember;
        } else {
            readMember = getReadMethod(entityType, propertyName);
            writeMember = getWriteMethod(entityType, propertyName);
        }
        if (!isAccessible(readMember) || !isAccessible(writeMember)) {
            return super.createPropertyAccessStrategy(classMapping, propertyName);
        }
        PropertyAccessStrategy propertyAccessStrategy = findPropertyAccessStrategy(className, propertyName);
        if (propertyAccessStrategy != null) {
            return propertyAccessStrategy;
        }
        return createPropertyAccessStrategy(className, propertyName, readMember, writeMember, usesFieldAccess);
    }

    private boolean isAccessible(Member member) {
        return !Modifier.isPrivate(member.getModifiers());
    }

    private String getPropertyAccessStrategyClassName(String className, String propertyName) {
        return className + capitalize(propertyName) + "PropertyAccessStrategy";
    }

    private PropertyAccessStrategy findPropertyAccessStrategy(String className, String propertyName) {
        try {
            String propertyAccessStrategyClassName = getPropertyAccessStrategyClassName(className, propertyName);
            Class<PropertyAccessStrategy> propertyAccessStrategyClass
                = (Class<PropertyAccessStrategy>)Class.forName(propertyAccessStrategyClassName);
            return ReflectionUtils.newInstance(propertyAccessStrategyClass);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private PropertyAccessStrategy createPropertyAccessStrategy(String className,
                                                                String propertyName,
                                                                Member readMember,
                                                                Member writeMember,
                                                                boolean usesFieldAccess) {
        return ReflectionUtils.newInstance(createPropertyAccessStrategyClass(className,
                                                                             propertyName,
                                                                             readMember,
                                                                             writeMember,
                                                                             usesFieldAccess));
    }

    private Class<PropertyAccessStrategy> createPropertyAccessStrategyClass(String className,
                                                                            String propertyName,
                                                                            Member readMember,
                                                                            Member writeMember,
                                                                            boolean usesFieldAccess) {
        try {
            String propertyAccessStrategyClassName = getPropertyAccessStrategyClassName(className, propertyName);
            ClassPool pool = ClassPool.getDefault();
            CtClass strategy = pool.makeClass(propertyAccessStrategyClassName);
            strategy.setInterfaces(new CtClass[] {pool.get(PropertyAccessStrategy.class.getName())});
            strategy.addConstructor(CtNewConstructor.defaultConstructor(strategy));
            CtMethod readMethod = CtNewMethod.make(getReadMethod(className, readMember, usesFieldAccess), strategy);
            strategy.addMethod(readMethod);
            CtMethod writeMethod = CtNewMethod.make(getWriteMethod(className, writeMember, usesFieldAccess), strategy);
            strategy.addMethod(writeMethod);
            return strategy.toClass();
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getReadMethod(String className, Member readMember, boolean usesFieldAccess) {
        return "public Object getPropertyValue(Object target) {"
             + "    return ($w)((" + className + ")$1)." + readMember.getName() + (usesFieldAccess? ";": "();")
             + "}";
    }

    private String getWriteMethod(String className, Member writeMember, boolean usesFieldAccess) {
        Class<?> propertyType = getPropertyType(writeMember, usesFieldAccess);
        return "public void setPropertyValue(Object target, Object value) {"
             + "    ((" + className + ")$1)." + writeMember.getName()
             +                              (usesFieldAccess? " = ": "") + "(" + getCastExpression(propertyType) + ");"
             + "}";
    }

    private Class<?> getPropertyType(Member writeMember, boolean usesFieldAccess) {
        return usesFieldAccess? ((Field)writeMember).getType(): ((Method)writeMember).getParameterTypes()[0];
    }

    private String getCastExpression(Class<?> propertyType) {
        if (propertyType.equals(Boolean.TYPE)) {
            return "((Boolean)$2).booleanValue()";
        } else if (propertyType.equals(Byte.TYPE)) {
            return "((Byte)$2).byteValue()";
        } else if (propertyType.equals(Character.TYPE)) {
            return "((Character)$2).charValue()";
        } else if (propertyType.equals(Short.TYPE)) {
            return "((Short)$2).shortValue()";
        } else if (propertyType.equals(Integer.TYPE)) {
            return "((Integer)$2).intValue()";
        } else if (propertyType.equals(Long.TYPE)) {
            return "((Long)$2).longValue()";
        } else if (propertyType.equals(Float.TYPE)) {
            return "((Float)$2).floatValue()";
        } else if (propertyType.equals(Double.TYPE)) {
            return "((Double)$2).doubleValue()";
        } else {
            return "(" + propertyType.getName() + ")$2";
        }
    }
}
