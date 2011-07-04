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

import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public class ReflectionFieldAccessStrategy implements PropertyAccessStrategy {

    private Field field;
    private BeanInitializer beanInitializer;

    public ReflectionFieldAccessStrategy(Field field, BeanInitializer beanInitializer) {
        this.field = field;
        this.beanInitializer = beanInitializer;
    }

    public Object getPropertyValue(Object target) {
        return ReflectionUtils.getFieldValue(field, beanInitializer.initialize(target));
    }

    public void setPropertyValue(Object target, Object value) {
        ReflectionUtils.setFieldValue(field, target, value);
    }
}
