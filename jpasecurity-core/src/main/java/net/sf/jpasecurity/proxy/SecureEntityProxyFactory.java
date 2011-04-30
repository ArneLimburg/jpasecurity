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
package net.sf.jpasecurity.proxy;

import net.sf.jpasecurity.SecureEntity;

/**
 * <tt>SecureEntityProxyFactory</tt> provides methods to create {@link SecureEntity}-proxies.
 *
 * @author Arne Limburg
 */
public interface SecureEntityProxyFactory {

    SecureEntity createSecureEntityProxy(Class<?> entityType,
                                         MethodInterceptor interceptor,
                                         Decorator<SecureEntity> decorator);
    MethodInterceptor getInterceptor(SecureEntity entity);
    Decorator<SecureEntity> getDecorator(SecureEntity entity);

}
