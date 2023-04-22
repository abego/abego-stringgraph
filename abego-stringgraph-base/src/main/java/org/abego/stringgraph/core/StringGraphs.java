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

import org.abego.stringgraph.internal.StringGraphsImpl;

import java.net.URI;
import java.util.function.Function;

public interface StringGraphs {

    static StringGraphs getInstance() {
        return StringGraphsImpl.getInstance();
    }

    StringGraphBuilder createStringGraphBuilder();

    /**
     * Writes the {@code stringGraph} to the give {@code uri}.
     */
    void writeStringGraph(StringGraph stringGraph, URI uri);

    /**
     * Reads the {@code stringGraph} from the give {@code uri} and returns it.
     */
    StringGraph readStringGraph(URI uri);

    /**
     * Reads ({@link StringGraph}-defining) data from the given {@code uri} and
     * calls the corresponding methods of the {@link StringGraphConstructing}
     * object to construct a {@link StringGraph}.
     * <p>
     * The same StringGraphConstructing object may be used with different
     * URIs, e.g. to create a "merged" StringGraph.
     *
     * @param uri          The {@link URI} to read from, formerly used with a
     *                     {@link #writeStringGraph(StringGraph, URI)} call to write a StringGraph
     * @param constructing a {@link StringGraphConstructing} instance used to
     *                     construct a StringGraph from the data read from the {@code uri}
     */
    void constructStringGraph(URI uri, StringGraphConstructing constructing);

    /**
     * Creates a {@link StringGraphDump} for the given {@code graph}, using the
     * {@code idOrLabelToText} function to translate the ids of {@link Node}s 
     * and labels of {@link Edge}s before dumping.
     */
    StringGraphDump createStringGraphDump(
            StringGraph graph, Function<String, String> idOrLabelToText);

    /**
     * Creates a {@link StringGraphDump} for the given {@code graph}.
     */
    StringGraphDump createStringGraphDump(StringGraph graph);
}
