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

package org.abego.stringgraph.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.net.URI;

import static org.abego.stringgraph.core.StringGraphTest.assertEqualToSample1;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringGraphsTest {

    @Test
    void createStringGraphBuilder() {
        StringGraphBuilder builder = StringGraphs.getInstance().createStringGraphBuilder();

        assertNotNull(builder);
    }

    @Test
    void writeReadConstructStringGraph(@TempDir File tempDir) {
        StringGraph graph = StringGraphTest.getSample1();
        File file = new File(tempDir, "sample.graph");
        URI uri = file.toURI();

        assertEqualToSample1(graph);
        // writeStringGraph
        StringGraphs.getInstance().writeStringGraph(graph, uri);

        // readStringGraph
        StringGraph readGraph = StringGraphs.getInstance().readStringGraph(uri);

        assertEqualToSample1(readGraph);

        // constructStringGraph
        StringGraphBuilder builder = StringGraphs.getInstance()
                .createStringGraphBuilder();

        StringGraphs.getInstance().constructStringGraph(uri, builder);
        
        StringGraph constructedGraph = builder.build();
        assertEqualToSample1(constructedGraph);
    }

}
