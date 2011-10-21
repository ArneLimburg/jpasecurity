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
package net.sf.jpasecurity.persistence.eclipselink;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.internal.jpa.deployment.JPAInitializer;
import org.eclipse.persistence.internal.jpa.deployment.JavaSECMPInitializer;

/**
 * This class implements a workaround for bug 361552 in eclipselink
 * ({@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=361552})
 * @author Arne Limburg
 */
public class PersistenceProvider extends org.eclipse.persistence.jpa.PersistenceProvider {

    public JPAInitializer getInitializer(String emName, @SuppressWarnings("rawtypes") Map m) {
        ClassLoader classLoader = getClassLoader(emName, m);
        return SecureJPAInitializer.getJavaSECMPInitializer(classLoader);
    }

    public static class SecureJPAInitializer extends JavaSECMPInitializer {

        public SecureJPAInitializer(ClassLoader classLoader) {
            super(classLoader);
        }

        public boolean isPersistenceProviderSupported(String providerClassName) {
            return true;
        }

        public static JavaSECMPInitializer getJavaSECMPInitializer(ClassLoader classLoader) {
            if (!isInitialized) {
                if (globalInstrumentation != null) {
                    synchronized (initializationLock) {
                        if (!isInitialized) {
                            initializeTopLinkLoggingFile();
                            usesAgent = true;
                            initializer = new SecureJPAInitializer(classLoader);
                            initializer.initialize(new HashMap<Object, Object>(0));
                            // all the transformers have been added to instrumentation, don't need it any more.
                            globalInstrumentation = null;
                        }
                    }
                }
                isInitialized = true;
            }
            if (initializer != null
                && initializer.getInitializationClassLoader() == classLoader
                && initializer instanceof SecureJPAInitializer) {
                return initializer;
            } else {
                return new SecureJPAInitializer(classLoader);
            }
        }
    }
}
