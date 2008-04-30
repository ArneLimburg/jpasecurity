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
package net.sf.jpasecurity.persistence.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Arne Limburg
 */
public class MappingInformation {

    private static final String CLASS_ENTRY_SUFFIX = ".class";

    private PersistenceUnitInfo persistenceUnit;
    private Map<Class<?>, ClassMappingInformation> entityTypeMappings = new HashMap<Class<?>, ClassMappingInformation>();
    private Map<String, ClassMappingInformation> entityNameMappings;
    private ClassLoader classLoader;
    private JpaAnnotationParser annotationParser = new JpaAnnotationParser(entityTypeMappings);

    public MappingInformation(PersistenceUnitInfo persistenceUnitInfo) {
        persistenceUnit = persistenceUnitInfo;
        parse();
    }
    
    public String getPersistenceUnitName() {
        return persistenceUnit.getPersistenceUnitName();
    }
    
    public Collection<Class<?>> getPersistentClasses() {
    	return Collections.unmodifiableSet(entityTypeMappings.keySet());
    }

    public ClassMappingInformation getClassMapping(Class<?> entityType) {
        ClassMappingInformation classMapping = entityTypeMappings.get(entityType);
        while (classMapping == null && entityType != null) {
            entityType = entityType.getSuperclass();
            classMapping = entityTypeMappings.get(entityType);
        }
        return classMapping;
    }
    
    public ClassMappingInformation getClassMapping(String entityName) {
        if (entityNameMappings == null) {
            initializeEntityNameMappings();
        }
        ClassMappingInformation classMapping = entityNameMappings.get(entityName);
        if (classMapping == null) {
        	throw new PersistenceException("Could not find mapping for entity with name \"" + entityName + '"');
        }
        return classMapping;
    }
    
    private void initializeEntityNameMappings() {
    	entityNameMappings = new HashMap<String, ClassMappingInformation>();
        for (ClassMappingInformation classMapping: entityTypeMappings.values()) {
            entityNameMappings.put(classMapping.getEntityName(), classMapping);
            entityNameMappings.put(classMapping.getEntityType().getName(), classMapping);
        }
    }
    
    private void parse() {
        classLoader = findClassLoader();
        if (persistenceUnit.getPersistenceUnitRootUrl() != null) {
            parse(persistenceUnit.getPersistenceUnitRootUrl());
        }
        for (URL url: persistenceUnit.getJarFileUrls()) {
            parse(url);
        }
        for (String className: persistenceUnit.getManagedClassNames()) {
            try {
                annotationParser.parse(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new PersistenceException(e);
            }
        }
        for (String mappingFilename: persistenceUnit.getMappingFileNames()) {
            parse(mappingFilename);
        }
        classLoader = null;
    }

    private void parse(URL url) {
        try {
            InputStream in = url.openStream();
            try {
                ZipInputStream zipStream = new ZipInputStream(in);
                for (ZipEntry entry = zipStream.getNextEntry(); entry != null; entry = zipStream.getNextEntry()) {
                    if (entry.getName().endsWith(CLASS_ENTRY_SUFFIX)) {
                        annotationParser.parse(classLoader.loadClass(entry.getName()));
                    }
                    zipStream.closeEntry();
                }
            } finally {
                in.close();
            }
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private void parse(String mappingFilename) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(classLoader.getResourceAsStream(mappingFilename));
            OrmXmlParser parser = new OrmXmlParser(entityTypeMappings, document);
            for (Node node: parser.getEntityNodes()) {
                parser.parse(classLoader.loadClass(getClassName(node)));
            }
            for (Node node: parser.getSuperclassNodes()) {
                parser.parse(classLoader.loadClass(getClassName(node)));
            }
            for (Node node: parser.getEmbeddableNodes()) {
                parser.parse(classLoader.loadClass(getClassName(node)));
            }
        } catch (ParserConfigurationException e) {
            throw new PersistenceException(e);
        } catch (SAXException e) {
            throw new PersistenceException(e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }
    
    private String getClassName(Node classNode) {
        Node classAttribute = classNode.getAttributes().getNamedItem(OrmXmlParser.CLASS_ATTRIBUTE_NAME);
        return classAttribute.getNodeValue();
    }

    private ClassLoader findClassLoader() {
        ClassLoader classLoader = persistenceUnit.getNewTempClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = persistenceUnit.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
