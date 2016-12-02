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
package org.jpasecurity.jpa;

import java.net.URL;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.jpasecurity.SecurityUnit;

/**
 * @author Arne Limburg
 */
public class JpaSecurityUnit implements SecurityUnit {

    private PersistenceUnitInfo persistenceUnit;

    public JpaSecurityUnit(PersistenceUnitInfo unit) {
        persistenceUnit = unit;
    }

    public ClassLoader getClassLoader() {
        return persistenceUnit.getClassLoader();
    }

    public String getSecurityUnitName() {
        return persistenceUnit.getPersistenceUnitName();
    }

    public List<String> getMappingFileNames() {
        return persistenceUnit.getMappingFileNames();
    }

    public boolean excludeUnlistedClasses() {
        return persistenceUnit.excludeUnlistedClasses();
    }

    public URL getSecurityUnitRootUrl() {
        return persistenceUnit.getPersistenceUnitRootUrl();
    }

    public List<URL> getJarFileUrls() {
        return persistenceUnit.getJarFileUrls();
    }

    public List<String> getManagedClassNames() {
        return persistenceUnit.getManagedClassNames();
    }
}
