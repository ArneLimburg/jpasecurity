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
package net.sf.jpasecurity.sample.simple;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SimpleSampleTest {

    private PrintStream originalOut;
    private BufferedReader out;

    @Before
    public void initializeOut() throws IOException {
        originalOut = System.out;
        PipedInputStream pipe = new PipedInputStream();
        out = new BufferedReader(new InputStreamReader(pipe));
        System.setOut(new PrintStream(new PipedOutputStream(pipe)));
    }

    public void resetOut() {
        System.setOut(originalOut);
    }

    @Test
    public void app() throws IOException {

        App.main(new String[0]);

        assertEquals("users.size = 1", out.readLine());
        assertEquals("contacts.size = 2", out.readLine());
    }
}
