/*
 * Copyright 2013 - 2016 Stefan Hildebrandt, Arne Limburg
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
package org.jpasecurity.persistence;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.jpasecurity.Path;
import org.jpasecurity.dto.IdAndNameDto;
import org.jpasecurity.dto.IdDto;
import org.junit.Test;

public class SecureQueryTest {

    @Test
    public void testHandleConstructorReturnTypePPMatchingType()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdAndNameDto> simpleDtoSecureQuery = createSecureQuery(IdAndNameDto.class);
        IdAndNameDto source = new IdAndNameDto(1, "simple");
        IdAndNameDto result = simpleDtoSecureQuery.handleConstructorReturnType(source);
        assertSame(source, result);
    }

    @Test
    public void testHandleConstructorReturnTypePPCompatibleParameterSingleValue()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdDto> simpleDtoSecureQuery = createSecureQuery(IdDto.class);
        IdDto result = simpleDtoSecureQuery.handleConstructorReturnType(1);
        assertEquals(1, result.getId().longValue());
    }

    @Test
    public void testHandleConstructorReturnTypePPCompatibleParameterArray()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdAndNameDto> simpleDtoSecureQuery = createSecureQuery(IdAndNameDto.class);
        IdAndNameDto result = simpleDtoSecureQuery.handleConstructorReturnType(new Object[]{1, "Test"});
        assertEquals(1, result.getId().longValue());
        assertEquals("Test", result.getName());
    }

    @Test
    public void testHandleConstructorReturnTypePPCompatibleParameterSingleValuedArray()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdDto> simpleDtoSecureQuery = createSecureQuery(IdDto.class);
        IdDto result = simpleDtoSecureQuery.handleConstructorReturnType(new Object[]{1});
        assertEquals(1, result.getId().longValue());
    }

    @Test(expected = PersistenceException.class)
    public void testHandleConstructorReturnTypePPInCompatibleParameterSingleValue()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdAndNameDto> simpleDtoSecureQuery = createSecureQuery(IdAndNameDto.class);
        simpleDtoSecureQuery.handleConstructorReturnType(1);
    }

    @Test(expected = PersistenceException.class)
    public void testHandleConstructorReturnTypePPIncompatibleParameterArray()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdDto> simpleDtoSecureQuery = createSecureQuery(IdDto.class);
        simpleDtoSecureQuery.handleConstructorReturnType(new Object[]{1, "Test"});
    }

    @Test(expected = PersistenceException.class)
    public void testHandleConstructorReturnTypePPNotMatchingTypeType()
        throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SecureQuery<IdDto> simpleDtoSecureQuery = new SecureQuery<IdDto>(
            mock(Query.class),
            IdDto.class,
            Collections.<Path>emptyList(),
            FlushModeType.AUTO
        );
        IdAndNameDto source = new IdAndNameDto(1, "simple");
        simpleDtoSecureQuery.handleConstructorReturnType(source);
    }

    private <T> SecureQuery<T> createSecureQuery(Class<T> queryClassType) {
        return new SecureQuery<T>(
            mock(Query.class),
            queryClassType,
            Collections.<Path>emptyList(),
            FlushModeType.AUTO
        );
    }
}
