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
package org.springframework.samples.petclinic.integrationtest.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author Arne Limburg
 */
public class ParameterizedJUnit4ClassRunner extends Suite {

    public ParameterizedJUnit4ClassRunner(Class<?> testClass) throws Exception {
        super(testClass, buildRunners(testClass));
    }

    private static List<Runner> buildRunners(Class<?> testClass) throws Exception {
        Parameters parameters = testClass.getAnnotation(Parameters.class);
        if (parameters == null) {
            throw new IllegalStateException("Missing annotation @Parameters at class " + testClass.getName());
        }
        List<Runner> runners = new ArrayList<Runner>();
        for (String parameter: parameters.value()) {
            runners.add(new ParameterValueJUnit4ClassRunner(testClass, parameter));
        }
        return runners;
    }

    private static class ParameterValueJUnit4ClassRunner extends BlockJUnit4ClassRunner {

        private String value;

        public ParameterValueJUnit4ClassRunner(Class<?> testClass, String value) throws Exception {
            super(testClass);
            this.value = value;
        }

        protected String getName() {
            return super.getName() + " [" + value + "]";
        }

        protected String testName(FrameworkMethod method) {
            return method.getName() + " [" + value + "]";
        }

        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
            validateStringArgConstructor(errors);
        }

        protected void validateStringArgConstructor(List<Throwable> errors) {
            if (getTestClass().getOnlyConstructor().getParameterTypes().length != 1
                || getTestClass().getOnlyConstructor().getParameterTypes()[0] != String.class) {
                String gripe = "Test class should have exactly one public constructor with one String-argument";
                errors.add(new Exception(gripe));
            }
        }

        protected Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(value);
        }
    }
}
