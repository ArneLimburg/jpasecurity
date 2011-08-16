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
package net.sf.jpasecurity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arne Limburg
 */
public class DefaultSecurityUnit implements SecurityUnit {

    private String name;
    private URL rootUrl;
    private boolean excludeUnlistedClasses;
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private List<URL> jarFileUrls = new ArrayList<URL>();
    private List<String> managedClassNames = new ArrayList<String>();
    private List<String> mappingFileNames = new ArrayList<String>();

    public DefaultSecurityUnit(String name) {
        this.name = name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getSecurityUnitName() {
        return name;
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public URL getSecurityUnitRootUrl() {
        return rootUrl;
    }

    public void setSecurityUnitRootUrl(URL url) {
        rootUrl = url;
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }
}
