/*
 * Copyright 2016 Arne Limburg
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

import static org.jpasecurity.util.Validate.notNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.w3c.dom.Node;

public class NamedQueryParser {

    private Metamodel metamodel;
    private Collection<String> ormXmlLocations;

    public NamedQueryParser(Metamodel metamodel, Collection<String> ormXmlLocations) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.ormXmlLocations = notNull("ormXmlLocations", ormXmlLocations);
    }

    public ConcurrentMap<String, String> parseNamedQueries() {
        ConcurrentMap<String, String> namedQueries = new ConcurrentHashMap<String, String>();
        for (ManagedType<?> managedType: metamodel.getManagedTypes()) {
            namedQueries.putAll(parseNamedQueries(managedType.getJavaType()));
        }
        namedQueries.putAll(parseNamedQueries(ormXmlLocations));
        return namedQueries;
    }

    private Map<String, String> parseNamedQueries(Class<?> type) {
        Map<String, String> parsedQueries = new HashMap<String, String>();
        NamedQuery namedQuery = type.getAnnotation(NamedQuery.class);
        if (namedQuery != null) {
            parsedQueries.put(namedQuery.name(), namedQuery.query());
        }
        NamedQueries namedQueries = type.getAnnotation(NamedQueries.class);
        if (namedQueries != null) {
            for (NamedQuery query: namedQueries.value()) {
                parsedQueries.put(query.name(), query.query());
            }
        }
        return parsedQueries;
    }

    private Map<String, String> parseNamedQueries(Collection<String> ormXmlLocations) {
        try {
            Map<String, String> parsedQueries = new HashMap<String, String>();
            XmlParser parser = new XmlParser(ormXmlLocations.toArray(new String[ormXmlLocations.size()]));
            for (Node query: parser.parseGlobalNamedQueries()) {
                parsedQueries.put(getNamedQueryName(query), query.getTextContent());
            }
            for (Node query: parser.parseEntityNamedQueries()) {
                parsedQueries.put(getEntityNamedQueryName(query), query.getTextContent());
            }
            return parsedQueries;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String getNamedQueryName(Node namedQuery) {
        return namedQuery.getParentNode().getAttributes().getNamedItem("name").getTextContent();
    }

    private String getEntityNamedQueryName(Node namedQuery) {
        Node entityNode = namedQuery.getParentNode().getParentNode();
        String entityName;
        Node nameAttribute = entityNode.getAttributes().getNamedItem("name");
        if (nameAttribute != null) {
            entityName = nameAttribute.getTextContent();
        } else {
            Node classAttribute = entityNode.getAttributes().getNamedItem("class");
            String className = classAttribute.getTextContent();
            entityName = className.substring(className.lastIndexOf('.') + 1);
        }
        return entityName + '.' + getNamedQueryName(namedQuery);
    }
}
