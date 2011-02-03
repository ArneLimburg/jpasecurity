/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.xml;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.persistence.PersistenceException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import net.sf.jpasecurity.configuration.DefaultExceptionFactory;

import org.easymock.IAnswer;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests the correct exception behavior of {@link AbstractXmlParser}.
 * @author Arne Limburg
 */
public class AbstractXmlParserTest extends TestCase {
    
    public static final String SAX_PARSER_FACTORY_PROPERTY_NAME = "javax.xml.parsers.SAXParserFactory";

    private String oldSaxParserFactoryName;
    
    public void setUp() {
        oldSaxParserFactoryName = System.getProperty(SAX_PARSER_FACTORY_PROPERTY_NAME);
        System.setProperty(SAX_PARSER_FACTORY_PROPERTY_NAME, TestSaxParserFactory.class.getName());
    }
    
    public void tearDown() {
        if (oldSaxParserFactoryName == null) {
            System.clearProperty(SAX_PARSER_FACTORY_PROPERTY_NAME);
        } else {
            System.setProperty(SAX_PARSER_FACTORY_PROPERTY_NAME, oldSaxParserFactoryName);
        }
    }
    
    public void testParserConfigurationException() {
        AbstractXmlParser<DefaultHandler> parser = new AbstractXmlParser<DefaultHandler>(null, new DefaultExceptionFactory()) {
        };
        try {
            TestSaxParserFactory.throwParserConfigurationException();
            parser.parse(new ByteArrayInputStream(new byte[1]));
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(ParserConfigurationException.class, e.getCause().getClass());
        } finally {
            TestSaxParserFactory.clear();
        }
    }
    
    public void testSaxException() {
        AbstractXmlParser<DefaultHandler> parser = new AbstractXmlParser<DefaultHandler>(null, new DefaultExceptionFactory()) {
        };
        try {
            parser.parse(new ByteArrayInputStream(new byte[1]));
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(SAXException.class, e.getCause().getClass());
        }
    }
    
    public void testIoException() {
        AbstractXmlParser<DefaultHandler> parser = new AbstractXmlParser<DefaultHandler>(null, new DefaultExceptionFactory()) {
        };
        try {
            parser.parse(new InputStream() {
                public int read() throws IOException {
                    throw new IOException();
                }
            });
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(IOException.class, e.getCause().getClass());
        }
    }
    
    public static class TestSaxParserFactory extends SAXParserFactory {

        private static final ThreadLocal<Boolean> throwParserConfigurationException = new ThreadLocal<Boolean>();

        public static void throwParserConfigurationException() {
            throwParserConfigurationException.set(true);
        }
        
        public static void clear() {
            throwParserConfigurationException.remove();
        }
        
        public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
            if (throwParserConfigurationException.get() != null && throwParserConfigurationException.get()) {
                throw new ParserConfigurationException();
            } else {
                return new TestSaxParser();
            }
        }

        public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
            return false;
        }

        public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
            throw new SAXNotSupportedException();
        }
    }
    
    private static class TestSaxParser extends SAXParser {

        public Parser getParser() throws SAXException {
            throw new SAXException();
        }

        public XMLReader getXMLReader() throws SAXException {
            try {
                XMLReader xmlReader = createMock(XMLReader.class);
                xmlReader.parse((InputSource)anyObject());
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    public Object answer() throws Throwable {
                        InputSource source = (InputSource)getCurrentArguments()[0];
                        source.getByteStream().read();
                        throw new SAXException();
                    }
                });
                replay(xmlReader);
                return xmlReader;
            } catch (IOException e) {
                throw new SAXException(e);
            }
        }

        public boolean isNamespaceAware() {
            return false;
        }

        public boolean isValidating() {
            return false;
        }
        
        public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            throw new SAXNotSupportedException();
        }

        public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
            throw new SAXNotSupportedException();
        }
    }
}
