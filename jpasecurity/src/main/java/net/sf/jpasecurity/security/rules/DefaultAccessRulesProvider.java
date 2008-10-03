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
package net.sf.jpasecurity.security.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;

/**
 * This implementation of the {@link AccessRulesProvider} interface.
 * @author Arne Limburg
 */
public class DefaultAccessRulesProvider implements AccessRulesProvider, PersistenceInformationReceiver {

    private final AnnotationAccessRulesProvider annotationRulesProvider = new AnnotationAccessRulesProvider();
    private final XmlAccessRulesProvider xmlRulesProvider = new XmlAccessRulesProvider();
    private List<AccessRule> accessRules;

    public List<AccessRule> getAccessRules() {
        if (accessRules == null) {
            List<AccessRule> accessRules = new ArrayList<AccessRule>();
            accessRules.addAll(annotationRulesProvider.getAccessRules());
            accessRules.addAll(xmlRulesProvider.getAccessRules());
            this.accessRules = Collections.unmodifiableList(accessRules);
        }
        return accessRules;
    }

    public void setPersistenceMapping(MappingInformation persistenceMapping) {
        annotationRulesProvider.setPersistenceMapping(persistenceMapping);
        xmlRulesProvider.setPersistenceMapping(persistenceMapping);
    }

    public void setPersistenceProperties(Map<String, String> properties) {
        annotationRulesProvider.setPersistenceProperties(properties);
        xmlRulesProvider.setPersistenceProperties(properties);
    }
}
