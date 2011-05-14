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
package net.sf.jpasecurity.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ReflectionUtilsTest {

    @Test
    public void newInstance() {

        assertTrue(ReflectionUtils.newInstance(ClassWithDefaultConstructor.class).isObjectConstructorCalled());

        try {
            ReflectionUtils.newInstance(ClassWithoutDefaultConstructor.class);
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertEquals(NoSuchMethodException.class, e.getCause().getClass());
        }

        Object o = new Object();
        String s = new String();
        ClassWithAmbigiousConstructors instance;

        instance = ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, o, o);
        assertTrue(instance.isObjectConstructorCalled());

        instance = ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, o, s);
        assertTrue(instance.isStringConstructorCalled());

        try {
            ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, o, s);
        } catch (SecurityException e) {
            assertEquals(NoSuchMethodException.class, e.getCause().getClass());
        }

        try {
            ReflectionUtils.newInstance(ClassWithAmbigiousConstructors.class, s, s, o);
        } catch (SecurityException e) {
            assertEquals(InstantiationException.class, e.getCause().getClass());
        }
    }

    public static class ReflectionUtilsTestClass {

        boolean objectConstructorCalled = false;

        public boolean isObjectConstructorCalled() {
            return objectConstructorCalled;
        }
    }

    public static class ClassWithDefaultConstructor extends ReflectionUtilsTestClass {

        public ClassWithDefaultConstructor() {
            objectConstructorCalled = true;
        }
    }

    public static class ClassWithoutDefaultConstructor extends ReflectionUtilsTestClass {

        public ClassWithoutDefaultConstructor(Object parameter) {
            objectConstructorCalled = true;
        }
    }

    public static class ClassWithAmbigiousConstructors extends ReflectionUtilsTestClass {

        private boolean stringConstructorCalled = false;

        public ClassWithAmbigiousConstructors(Object parameter1, Object parameter2, Object parameter3) {
            objectConstructorCalled = true;
        }

        public ClassWithAmbigiousConstructors(String parameter1, Object parameter2, Object parameter3) {
        }

        public ClassWithAmbigiousConstructors(Object parameter1, String parameter2, Object parameter3) {
        }

        public ClassWithAmbigiousConstructors(String parameter1, String parameter2, Object parameter3) {
        }

        public ClassWithAmbigiousConstructors(Object parameter1, Object parameter2, String parameter3) {
            stringConstructorCalled = true;
        }

        public boolean isStringConstructorCalled() {
            return stringConstructorCalled;
        }
    }
}
