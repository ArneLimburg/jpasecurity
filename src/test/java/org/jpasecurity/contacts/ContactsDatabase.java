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
package org.jpasecurity.contacts;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;

/**
 * @author Arne Limburg
 */
@Singleton
public class ContactsDatabase {

    private static final String CREATE_USER_TABLE
        = "CREATE TABLE USR (ID INTEGER NOT NULL, NAME VARCHAR(255), PRIMARY KEY (ID))";
    private static final String CREATE_CONTACT_TABLE
        = "CREATE TABLE CONTACT (ID INTEGER NOT NULL, TEXT VARCHAR(255), OWNER_ID INTEGER, PRIMARY KEY (ID))";
    private static final String ADD_FOREIGN_KEY
        = "ALTER TABLE CONTACT ADD CONSTRAINT FK_CONTACT_OWNER_ID FOREIGN KEY (OWNER_ID) REFERENCES USR (ID)";
    private static final String CREATE_GLOBAL_SEQUENCE
        = "CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT NUMERIC, PRIMARY KEY (SEQ_NAME))";
    private static final String INITIALIZE_GLOBAL_SEQUENCE
        = "INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) values ('SEQ_GEN', 0)";

    @PostConstruct
    public void create() {
        DataSource dataSource;
        try {
            dataSource = (DataSource)new InitialContext().lookup("jdbc/__default");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(CREATE_USER_TABLE);
            statement.execute(CREATE_CONTACT_TABLE);
            statement.execute(ADD_FOREIGN_KEY);
            statement.execute(CREATE_GLOBAL_SEQUENCE);
            statement.execute(INITIALIZE_GLOBAL_SEQUENCE);
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }
}
