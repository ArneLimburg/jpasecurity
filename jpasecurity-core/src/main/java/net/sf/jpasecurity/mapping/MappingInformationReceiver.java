/*
 * Copyright 2008 Arne Limburg
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

import java.util.Map;

/**
 * This interface may be implemented by {@link net.net.sf.jpasecurity.configuration.AuthenticationProvider}s
 * and {@link net.sf.jpasecurity.configuration.net.sf.jpasecurity.security.AccessRulesProvider}s
 * to obtain the specified persistence informations.
 *
 * @author Arne Limburg
 */
public interface MappingInformationReceiver {

    void setMappingInformation(MappingInformation persistenceMapping);
    void setMappingProperties(Map<String, Object> properties);
}
