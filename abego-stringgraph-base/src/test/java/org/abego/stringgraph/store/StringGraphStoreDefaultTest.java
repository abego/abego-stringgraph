/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.store;

import org.abego.commons.io.FileUtil;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphException;
import org.abego.stringgraph.core.StringGraphTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringGraphStoreDefaultTest {
    @Test
    void writeReadStringGraph(@TempDir File tempDir) {
        StringGraph graph = StringGraphTest.getSample1();
        File file = new File(tempDir, "sample.graph");

        StringGraphStore stringGraphStore = StringGraphStores.getStringGraphStore(file.toURI());
        stringGraphStore.writeStringGraph(graph);
        StringGraph readGraph = stringGraphStore.readStringGraph();

        StringGraphTest.assertEqualToSample1(readGraph);
    }

    @Test
    void readExceptions() throws IOException {
        StringGraphStoreDefault store =
                StringGraphStoreDefault.createStringGraphStoreDefault(new File("foo").toURI());
        //noinspection resource
        ObjectInputStream stream = new ObjectInputStream() {
            @Override
            protected Object readObjectOverride() throws IOException {
                throw new IOException();
            }
        };
        
        StringGraphStoreException ex =
                assertThrows(StringGraphStoreException.class,
                        () -> store.readTag(stream));
        assertEquals("Error when reading block tag", ex.getMessage());
        ex =
                assertThrows(StringGraphStoreException.class,
                        () -> store.readInt(stream));
        assertEquals("Error when reading VLQ 'int'", ex.getMessage());
        ex =
                assertThrows(StringGraphStoreException.class,
                        () -> store.readEndBlock(stream,null));
        assertEquals("Error when reading VLQ 'int'", ex.getMessage());
        ex =
                assertThrows(StringGraphStoreException.class,
                        () -> store.readStringGraph(stream,null));
        assertEquals("Error when reading data format name", ex.getMessage());
    }

    @Test
    void writeExceptions() throws IOException {
        StringGraphStoreDefault store =
                StringGraphStoreDefault.createStringGraphStoreDefault(new File("foo").toURI());
        
        //noinspection resource
        ObjectOutputStream stream = new ObjectOutputStream() {
            @Override
            protected void writeObjectOverride(Object obj) throws IOException {
                throw new IOException();
            }
        };

        StringGraphStoreException e =
                assertThrows(StringGraphStoreException.class,
                        () -> store.writeTag(stream, "foo"));
        assertEquals("Error when writing block tag", e.getMessage());

        e = assertThrows(StringGraphStoreException.class,
                () -> store.writeEndBlock(stream));
        assertEquals("Error when writing VLQ 'int'", e.getMessage());
    }


    @Test
    void readMissingFile(@TempDir File tempDir) {
        File file = new File(tempDir, "missing.graph");

        assertThrows(StringGraphStoreException.class,
                () -> StringGraphStoreDefault.createStringGraphStoreDefault(file.toURI())
                        .readStringGraph());
    }

    @Test
	@DisabledOnOs(OS.WINDOWS)  // Windows cannot delete tempDir because file is in use
    void readEmptyFile(@TempDir File tempDir) {
        File file = new File(tempDir, "empty");
        FileUtil.ensureFileExists(file);

        StringGraphException e = assertThrows(StringGraphException.class,
                () -> StringGraphStoreDefault.createStringGraphStoreDefault(file.toURI())
                        .readStringGraph());
        assertTrue(e.getMessage().startsWith("Error when reading graph from "));
    }

    @Test
	@DisabledOnOs(OS.WINDOWS)  // Windows cannot delete tempDir because file is in use
    void readFileWithWrongDataFormatName(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "some.graph");
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(Files.newOutputStream(file.toPath()));
        objectOutputStream.writeObject("wrong header");
        objectOutputStream.close();

        StringGraphException e = assertThrows(StringGraphException.class,
                () -> StringGraphStoreDefault.createStringGraphStoreDefault(file.toURI())
                        .readStringGraph());
        assertEquals("Invalid file format. Expected header 'org.abego.stringgraph.store.StringGraphStoreDefault', got 'wrong header'", e.getMessage());
    }

    @Test
	@DisabledOnOs(OS.WINDOWS)  // Windows cannot delete tempDir because file is in use
    void readFileWithWrongDataFormatVersion(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "some.graph");
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(Files.newOutputStream(file.toPath()));
        objectOutputStream.writeObject(StringGraphStoreDefault.getDataFormatName());
        objectOutputStream.writeInt(StringGraphStoreDefault.getDataFormatVersion().majorNumber + 1);
        objectOutputStream.writeInt(StringGraphStoreDefault.getDataFormatVersion().minorNumber);
        objectOutputStream.close();

        StringGraphException e = assertThrows(StringGraphException.class,
                () -> StringGraphStoreDefault.createStringGraphStoreDefault(file.toURI())
                        .readStringGraph());
        assertEquals("Incompatible data format version. Expected '1', got '2'", e.getMessage());
    }

    @Test
    void constructStringGraphFromFile(@TempDir File tempDir) {
        StringGraph graph = StringGraphTest.getSample1();
        StringGraphException e = assertThrows(StringGraphException.class,
                () -> StringGraphStores.getStringGraphStore(tempDir.toURI()).writeStringGraph(graph));
        assertTrue(e.getMessage().startsWith("Error when writing graph to "));
    }    
}
