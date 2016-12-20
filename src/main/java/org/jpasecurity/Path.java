/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity;

import static org.jpasecurity.util.Validate.notNull;

/**
 * A class representing a JPQL path.
 * @author Arne Limburg
 */
public class Path {

    private static final String[] EMPTY = new String[0];
    private static final String KEY_FUNCTION = "KEY(";
    private static final String VALUE_FUNCTION = "VALUE(";
    private static final String ENTRY_FUNCTION = "ENTRY(";

    private Alias rootAlias;
    private boolean isKeyPath;
    private boolean isValuePath;
    private String subpath;
    private String[] subpathComponents;

    public Path(String path) {
        int index = path.indexOf('.');
        String firstPathSegment;
        if (index == -1) {
            firstPathSegment = path.trim();
            subpath = null;
        } else {
            firstPathSegment = path.substring(0, index).trim();
            subpath = path.substring(index + 1).trim();
        }
        if (isKeySegment(firstPathSegment)) {
            isKeyPath = true;
            rootAlias = new Alias(firstPathSegment.substring(KEY_FUNCTION.length(), firstPathSegment.length() - 1));
        } else if (isValueSegment(firstPathSegment)) {
            isValuePath = true;
            rootAlias = new Alias(firstPathSegment.substring(VALUE_FUNCTION.length(), firstPathSegment.length() - 1));
        } else if (isEntrySegment(firstPathSegment)) {
            rootAlias = new Alias(firstPathSegment.substring(ENTRY_FUNCTION.length(), firstPathSegment.length() - 1));
        } else {
            rootAlias = new Alias(firstPathSegment);
        }
    }

    public Path(Path path) {
        this(path.getRootAlias(), path.getSubpath());
    }

    protected Path(Alias alias, String path) {
        notNull(Alias.class, alias);
        rootAlias = alias;
        subpath = path;
    }

    public boolean isKeyPath() {
        return isKeyPath;
    }

    public boolean isValuePath() {
        return isValuePath;
    }

    public boolean hasParentPath() {
        return hasSubpath();
    }

    public Path getParentPath() {
        int index = subpath.lastIndexOf('.');
        if (index == -1) {
            return new Path(rootAlias, null);
        } else {
            return new Path(rootAlias, subpath.substring(0, index));
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

    public String[] getPathComponents() {
        String[] subpathComponents = getSubpathComponents();
        String[] components = new String[subpathComponents.length + 1];
        components[0] = rootAlias.toString();
        System.arraycopy(subpathComponents, 0, components, 1, subpathComponents.length);
        return components;
    }

    public String[] getSubpathComponents() {
        if (subpathComponents == null) {
            subpathComponents = hasSubpath()? subpath.split("\\."): EMPTY;
        }
        return subpathComponents;
    }

    public Path append(String name) {
        return new Path(toString() + '.' + name);
    }

    public boolean isEnumValue() {
        return getEnumValue() != null;
    }

    public Enum getEnumValue() {
        String pathName = toString();
        int lastDot = pathName.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        String className = pathName.substring(0, lastDot);
        Class<? extends Enum> enumType = loadEnumClass(className);
        if (enumType == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, pathName.substring(lastDot + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isKeyPath()) {
            builder.append(KEY_FUNCTION).append(rootAlias.getName()).append(')');
        } else if (isValuePath()) {
            builder.append(VALUE_FUNCTION).append(rootAlias.getName()).append(')');
        } else {
            builder.append(rootAlias.getName());
        }
        if (hasSubpath()) {
            builder.append('.').append(getSubpath());
        }
        return builder.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Path)) {
            return false;
        }
        return toString().equals(object.toString());
    }

    private boolean isKeySegment(String rootSegment) {
        return rootSegment.toUpperCase().startsWith(KEY_FUNCTION);
    }

    private boolean isValueSegment(String rootSegment) {
        return rootSegment.toUpperCase().startsWith(VALUE_FUNCTION);
    }

    private boolean isEntrySegment(String rootSegment) {
        return rootSegment.toUpperCase().startsWith(ENTRY_FUNCTION);
    }

    private Class<? extends Enum> loadEnumClass(String className) {
        try {
            return (Class<? extends Enum>)Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
