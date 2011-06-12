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
package net.sf.jpasecurity.mapping;

/**
 * A class representing a JPQL path.
 * @author Arne Limburg
 */
public class Path {

    private Alias rootAlias;
    private String subpath;

    public Path(String path) {
        int index = path.indexOf('.');
        if (index == -1) {
            rootAlias = new Alias(path);
            subpath = null;
        } else {
            rootAlias = new Alias(path.substring(0, index));
            subpath = path.substring(index + 1);
        }
    }

    public boolean hasSubpath() {
        return subpath != null;
    }

    public Alias getRootAlias() {
        return rootAlias;
    }

    public String getSubpath() {
        return subpath;
    }
}
