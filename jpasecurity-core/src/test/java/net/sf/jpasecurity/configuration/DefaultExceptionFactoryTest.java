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
package net.sf.jpasecurity.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sf.jpasecurity.ExceptionFactory;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class DefaultExceptionFactoryTest {

    private ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
    private Exception cause = new Exception();
    private String message = "message";

    @Test
    public void createRuntimeException() {
        RuntimeException runtimeException = exceptionFactory.createRuntimeException(message, cause);
        assertTrue(runtimeException instanceof IllegalStateException);
        assertEquals(message, runtimeException.getMessage());
        assertEquals(cause, runtimeException.getCause());
    }
}
