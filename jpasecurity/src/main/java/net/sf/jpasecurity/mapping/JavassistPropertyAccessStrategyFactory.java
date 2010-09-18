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

import java.lang.reflect.Method;

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
        if (classMapping.usesFieldAccess()) {
            return super.createPropertyAccessStrategy(classMapping, propertyName);
        } else {
            Method readMethod = getReadMethod(classMapping.getEntityType(), propertyName);
            Method writeMethod = getWriteMethod(classMapping.getEntityType(), propertyName);
            Class<?> propertyType = writeMethod.getParameterTypes()[0];
            return ReflectionUtils.instantiate(createMethodAccessStrategyClass(classMapping.getEntityType().getName(),
                                                                               propertyType,
                                                                               propertyName,
                                                                               readMethod.getName(),
                                                                               writeMethod.getName()));
        }
    }

    private Class<PropertyAccessStrategy> createMethodAccessStrategyClass(String className,
                                                                          Class<?> propertyType,
                                                                          String propertyName,
                                                                          String readMethodName,
                                                                          String writeMethodName) {
        String propertyAccessStrategyClassName = className + capitalize(propertyName) + "PropertyAccessStrategy";
        ClassPool pool = ClassPool.getDefault();
        try {
            try {
                return (Class<PropertyAccessStrategy>)Class.forName(propertyAccessStrategyClassName);
            } catch (ClassNotFoundException notFoundException) {
                try {
                    CtClass strategy = pool.makeClass(propertyAccessStrategyClassName);
                    strategy.setInterfaces(new CtClass[] {pool.get(PropertyAccessStrategy.class.getName())});
                    strategy.addConstructor(CtNewConstructor.defaultConstructor(strategy));
                    CtMethod readMethod = CtNewMethod.make(getReadMethodBody(className, readMethodName), strategy);
                    strategy.addMethod(readMethod);
                    CtMethod writeMethod
                        = CtNewMethod.make(getWriteMethodBody(className, writeMethodName, propertyType), strategy);
                    strategy.addMethod(writeMethod);
                    return strategy.toClass();
                } catch (NotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getReadMethodBody(String className, String readMethodName) {
        return "public Object getPropertyValue(Object target) {"
             + "    return ($w)((" + className + ")$1)." + readMethodName + "();"
             + "}";
    }

    private String getWriteMethodBody(String className, String writeMethodName, Class<?> propertyType) {
        return "public void setPropertyValue(Object target, Object value) {"
             + "    ((" + className + ")$1)." + writeMethodName + "(" + getCastExpression(propertyType) + ");"
             + "}";
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
