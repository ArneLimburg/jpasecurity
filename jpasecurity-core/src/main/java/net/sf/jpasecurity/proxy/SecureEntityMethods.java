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
package net.sf.jpasecurity.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.SecureEntity;

/**
 * @author Arne Limburg
 */
public class SecureEntityMethods {

    private static final Map<String, List<Class<?>>> ENTRIES;
    static {
        Map<String, List<Class<?>>> secureEntityMethods = new HashMap<String, List<Class<?>>>();
        for (Method method: SecureEntity.class.getMethods()) {
            if (secureEntityMethods.containsKey(method.getName())) {
                String message
                    = SecureEntity.class.getName()
                    + " must not have more than one method with name " + method.getName();
                throw new IllegalStateException(message);
            }
            if (method.getParameterTypes().length == 0) {
                secureEntityMethods.put(method.getName(), Collections.<Class<?>>emptyList());
            } else {
                List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
                secureEntityMethods.put(method.getName(), Collections.unmodifiableList(parameterTypes));
            }
        }
        ENTRIES = Collections.unmodifiableMap(secureEntityMethods);
    }

    public static boolean contains(Method method) {
        List<Class<?>> secureEntityMethodParameterTypes = ENTRIES.get(method.getName());
        if (secureEntityMethodParameterTypes == null) {
            return false;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (secureEntityMethodParameterTypes.size() != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (secureEntityMethodParameterTypes.get(i) != parameterTypes[i]) {
                return false;
            }
        }
        return true;
    }
}
