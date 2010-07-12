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
            return ReflectionUtils.instantiate(createMethodAccessStrategyClass(classMapping.getEntityType().getName(),
                                                                               writeMethod.getParameterTypes()[0].getName(),
                                                                               propertyName,
                                                                               readMethod.getName(),
                                                                               writeMethod.getName()));
        }
    }
    
    private Class<PropertyAccessStrategy> createMethodAccessStrategyClass(String className,
                                                                          String propertyType,
                                                                          String propertyName,
                                                                          String readMethodName,
                                                                          String writeMethodName) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass strategy = pool.makeClass(className + capitalize(propertyName) + "PropertyAccessStrategy");
            strategy.setInterfaces(new CtClass[] {pool.get(PropertyAccessStrategy.class.getName())});
            CtMethod readMethod = CtNewMethod.make(getReadMethodBody(className, readMethodName), strategy);
            strategy.addMethod(readMethod);
            CtMethod writeMethod
                = CtNewMethod.make(getWriteMethodBody(className, writeMethodName, propertyType), strategy);
            strategy.addMethod(writeMethod);
            return strategy.toClass();
        } catch (CannotCompileException e) {
            throw new IllegalStateException(e);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private String getReadMethodBody(String className, String readMethodName) {
        return "public Object getPropertyValue(Object target) {"
             + "    return ((" + className + ")target)." + readMethodName + "();"
             + '}';
    }

    private String getWriteMethodBody(String className, String writeMethodName, String propertyTypeName) {
        return "public void setPropertyValue(Object target, Object value) {"
             + "    ((" + className + ")target)." + writeMethodName + "((" + propertyTypeName + ")value);"
             + '}';
    }
}
