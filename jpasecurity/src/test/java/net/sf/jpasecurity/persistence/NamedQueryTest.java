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
package net.sf.jpasecurity.persistence;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.mapping.ClassMappingInformation;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 * @author Johannes Siemer
 */
public class NamedQueryTest extends TestCase {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";
    
    public void testCreateNamedQuery() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean bean1 = new FieldAccessAnnotationTestBean(USER1);
        entityManager.persist(bean1);
        FieldAccessAnnotationTestBean bean2 = new FieldAccessAnnotationTestBean(USER2);
        entityManager.persist(bean2);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        List<FieldAccessAnnotationTestBean> result = entityManager.createNamedQuery("findAll").getResultList();
        assertEquals(1, result.size()); //the other bean is not accessible
        assertEquals(bean1.getIdentifier(), result.get(0).getIdentifier());
        entityManager.getTransaction().commit();
        entityManager.close();
    }
    
    public void testParseAllNamedQueries() throws Exception {
    	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	Document document = db.parse("src/test/resources/META-INF/orm.xml");
    	HashMap<String, String> namedQueryMap = new HashMap<String, String>();
		OrmXmlParser parser = new OrmXmlParser(new HashMap<Class<?>, ClassMappingInformation>(), namedQueryMap, document);
    	parser.parseNamedQueries();
    	
    	assertEquals(4, namedQueryMap.size());
    	
    	assertEquals("select test from Contact test", namedQueryMap.get("myQuery1"));
    	assertEquals("select test from Contact test", namedQueryMap.get("myQuery2"));
    	assertEquals("select test from Contact test", namedQueryMap.get("myQuery3"));
    	assertEquals("select test from Contact test", namedQueryMap.get("myQuery4"));
    }
    
    public void tearDown() {
        TestAuthenticationProvider.authenticate(null);
    }
}
