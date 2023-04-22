/*
 * MIT License
 *
 * Copyright (c) 2023 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphBuilder;
import org.abego.stringgraph.core.StringGraphConstructing;
import org.abego.stringgraph.core.StringGraphDump;
import org.abego.stringgraph.core.StringGraphs;

import java.net.URI;
import java.util.function.Function;

import static org.abego.stringgraph.internal.StringGraphStoreDefault.createStringGraphStoreDefault;

public class StringGraphsImpl implements StringGraphs {
    private static final StringGraphs INSTANCE = new StringGraphsImpl();

    public static StringGraphs getInstance() {
        return INSTANCE;
    }
    
    @Override
    public StringGraphBuilder createStringGraphBuilder() {
        return StringGraphBuilderImpl.createStringGraphBuilder();
    }

    @Override
    public void writeStringGraph(StringGraph stringGraph, URI uri) {
        StringGraphStoreDefault store = createStringGraphStoreDefault(uri);
        store.writeStringGraph(stringGraph);
    }

    @Override
    public StringGraph readStringGraph(URI uri) {
        StringGraphStoreDefault store = createStringGraphStoreDefault(uri);
        return store.readStringGraph();
    }

    @Override
    public void constructStringGraph(URI uri, StringGraphConstructing constructing) {
        StringGraphStoreDefault store = createStringGraphStoreDefault(uri);
        store.constructStringGraph(constructing);
    }

    @Override
    public StringGraphDump createStringGraphDump(
            StringGraph graph, Function<String, String> idOrLabelToText) {
        return StringGraphDumpImpl.createStringGraphDump(graph, idOrLabelToText);
    }

    @Override
    public StringGraphDump createStringGraphDump(StringGraph graph) {
        return StringGraphDumpImpl.createStringGraphDump(graph);
    }
}
