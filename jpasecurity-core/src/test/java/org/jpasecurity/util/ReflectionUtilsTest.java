/*
 * Copyright 2011 Arne Limburg
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
package org.jpasecurity.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ReflectionUtilsTest {

    @Test
    public void newInstance() {

        assertEquals(new ClassWithDefaultConstructor().getConstructor(),
                     ReflectionUtils.newInstance(ClassWithDefaultConstructor.class).getConstructor());

        try {
            ReflectionUtils.newInstance(ClassWithoutDefaultConstructor.class);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(NoSuchMethodException.class, e.getCause().getClass());
        }

        Object o = new Object();
        String s = new String();

        assertEquals(new ClassWithAmbigiousConstructors(o, o, o).getConstructor(),
                     ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, o, o).getConstructor());

        assertEquals(new ClassWithAmbigiousConstructors(s, o, o).getConstructor(),
                     ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, s, o, o).getConstructor());

        assertEquals(new ClassWithAmbigiousConstructors(s, s, o).getConstructor(),
                     ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, s, s, o).getConstructor());

        assertEquals(new ClassWithAmbigiousConstructors(o, o, s).getConstructor(),
                     ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, o, s).getConstructor());

        assertEquals(new ClassWithAmbigiousConstructors(s, null, o).getConstructor(),
                     ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, s, null, o).getConstructor());

        try {
            ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, s);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(NoSuchMethodException.class, e.getCause().getClass());
        }

        try {
            ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, s, o, s);
            fail("expected InstantiationError");
        } catch (InstantiationError e) {
            //expected
        }
    }

    public static class ReflectionUtilsTestClass {

        Constructor<?> calledConstructor;

        void setCalledConstructor(Class<?> type, Class<?>... parameterTypes) {
            try {
                calledConstructor = type.getConstructor(parameterTypes);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No matching constructor found for parameter types "
                                                   + Arrays.asList(parameterTypes));
            }
        }

        public Constructor<?> getConstructor() {
            return calledConstructor;
        }
    }

    public static class ClassWithDefaultConstructor extends ReflectionUtilsTestClass {

        public ClassWithDefaultConstructor() {
            setCalledConstructor(ClassWithDefaultConstructor.class);
        }
    }

    public static class ClassWithoutDefaultConstructor extends ReflectionUtilsTestClass {

        public ClassWithoutDefaultConstructor(Object parameter) {
            setCalledConstructor(ClassWithoutDefaultConstructor.class, Object.class);
        }
    }

    public static class ClassWithAmbigiousConstructors extends ReflectionUtilsTestClass {

        public ClassWithAmbigiousConstructors(Object parameter1, Object parameter2, Object parameter3) {
            setCalledConstructor(ClassWithAmbigiousConstructors.class, Object.class, Object.class, Object.class);
        }

        public ClassWithAmbigiousConstructors(String parameter1, Object parameter2, Object parameter3) {
            setCalledConstructor(ClassWithAmbigiousConstructors.class, String.class, Object.class, Object.class);
        }

        public ClassWithAmbigiousConstructors(Object parameter1, String parameter2, Object parameter3) {
            setCalledConstructor(ClassWithAmbigiousConstructors.class, Object.class, String.class, Object.class);
        }

        public ClassWithAmbigiousConstructors(String parameter1, String parameter2, Object parameter3) {
            setCalledConstructor(ClassWithAmbigiousConstructors.class, String.class, String.class, Object.class);
        }

        public ClassWithAmbigiousConstructors(Object parameter1, Object parameter2, String parameter3) {
            setCalledConstructor(ClassWithAmbigiousConstructors.class, Object.class, Object.class, String.class);
        }
    }
}
