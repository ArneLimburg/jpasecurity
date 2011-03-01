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
package net.sf.jpasecurity.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.persistence.PersistenceException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Arne Limburg
 */
public abstract class AbstractXmlParser<H extends DefaultHandler> {

    private H handler;

    public AbstractXmlParser(H xmlHandler) {
        handler = xmlHandler;
    }

    protected H getHandler() {
        return handler;
    }

    public void parse(URL url) throws IOException {
        InputStream stream = url.openStream();
        try {
            parse(stream);
        } finally {
            stream.close();
        }
    }

    public void parse(InputStream xml) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(xml, handler);
        } catch (ParserConfigurationException e) {
            throw new PersistenceException(e);
        } catch (SAXException e) {
            throw new PersistenceException(e);
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }
}
