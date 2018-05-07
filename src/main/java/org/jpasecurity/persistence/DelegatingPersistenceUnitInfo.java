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
package org.jpasecurity.persistence;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

/**
 * @author Arne Limburg
 */
public class DelegatingPersistenceUnitInfo implements PersistenceUnitInfo {

    private PersistenceUnitInfo delegate;

    DelegatingPersistenceUnitInfo(PersistenceUnitInfo delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate may not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public String getPersistenceUnitName() {
        return delegate.getPersistenceUnitName();
    }

    @Override
    public String getPersistenceProviderClassName() {
        return delegate.getPersistenceProviderClassName();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return delegate.getTransactionType();
    }

    @Override
    public DataSource getJtaDataSource() {
        return delegate.getJtaDataSource();
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return delegate.getNonJtaDataSource();
    }

    @Override
    public List<String> getMappingFileNames() {
        return delegate.getMappingFileNames();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return delegate.getJarFileUrls();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return delegate.getPersistenceUnitRootUrl();
    }

    @Override
    public List<String> getManagedClassNames() {
        return delegate.getManagedClassNames();
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return delegate.excludeUnlistedClasses();
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return delegate.getSharedCacheMode();
    }

    @Override
    public ValidationMode getValidationMode() {
        return delegate.getValidationMode();
    }

    @Override
    public Properties getProperties() {
        return delegate.getProperties();
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return delegate.getPersistenceXMLSchemaVersion();
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        delegate.addTransformer(transformer);
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return delegate.getNewTempClassLoader();
    }
}
