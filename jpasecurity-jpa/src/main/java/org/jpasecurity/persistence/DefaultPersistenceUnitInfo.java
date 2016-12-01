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
package org.jpasecurity.persistence;

import java.net.URL;
import java.util.ArrayList;
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
public class DefaultPersistenceUnitInfo implements PersistenceUnitInfo {

    private String persistenceUnitName;
    private String persistenceProviderClassName;
    private List<String> managedClassNames = new ArrayList<String>();
    private URL persistenceUnitRootUrl;
    private List<URL> jarFileUrls = new ArrayList<URL>();
    private List<String> mappingFileNames = new ArrayList<String>();
    private boolean excludeUnlistedClasses;
    private PersistenceUnitTransactionType persistenceUnitTransactionType;
    private String jtaDataSourceJndiName;
    private String nonJtaDataSourceJndiName;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;
    private Properties properties = new Properties();
    private ClassLoader classLoader;
    private ClassLoader tempClassLoader;
    private List<ClassTransformer> classTransformers = new ArrayList<ClassTransformer>();
    private ValidationMode validationMode;
    private SharedCacheMode sharedCacheMode;
    private String persistenceXMLSchemaVersion;

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String name) {
        persistenceUnitName = name;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(String name) {
        persistenceProviderClassName = name;
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public void setPersistenceUnitRootUrl(URL url) {
        persistenceUnitRootUrl = url;
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean exclude) {
        excludeUnlistedClasses = exclude;
    }

    public String getJtaDataSourceJndiName() {
        return jtaDataSourceJndiName;
    }

    public void setJtaDataSourceJndiName(String jndiName) {
        jtaDataSourceJndiName = jndiName;
    }

    public String getNonJtaDataSourceJndiName() {
        return nonJtaDataSourceJndiName;
    }

    public void setNonJtaDataSourceJndiName(String jndiName) {
        nonJtaDataSourceJndiName = jndiName;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitTransactionType;
    }

    public void setPersistenceUnitTransactionType(PersistenceUnitTransactionType type) {
        persistenceUnitTransactionType = type;
    }

    public void setJtaDataSource(DataSource dataSource) {
        jtaDataSource = dataSource;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource dataSource) {
        nonJtaDataSource = dataSource;
    }

    public Properties getProperties() {
        return properties;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    public ClassLoader getNewTempClassLoader() {
        return tempClassLoader;
    }

    public void setNewTempClassLoader(ClassLoader tempLoader) {
        tempClassLoader = tempLoader;
    }

    public void addTransformer(ClassTransformer transformer) {
        classTransformers.add(transformer);
    }

    public SharedCacheMode getSharedCacheMode() {
        return sharedCacheMode;
    }

    public void setSharedCacheMode(SharedCacheMode mode) {
        sharedCacheMode = mode;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode mode) {
        validationMode = mode;
    }

    public String getPersistenceXMLSchemaVersion() {
        return persistenceXMLSchemaVersion;
    }

    public void setPersistenceXMLSchemaVersion(String version) {
        persistenceXMLSchemaVersion = version;
    }
}
