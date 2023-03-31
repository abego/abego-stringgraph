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

import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphConstructing;

/**
 *  A store that holds a StringGraph.
 */
public interface StringGraphStore {

    /**
     * Writes the {@code stringGraph} to the store.
     */
    void writeStringGraph(StringGraph stringGraph);

    /**
     * Reads the {@link StringGraph} from the store and returns it.
     * <p>
     * The StringGraph is constructed using an efficient internal implementation.
     * For more control over the StringGraph construction use the method
     * {@link #readStringGraph(StringGraphConstructing)}.
     */
    StringGraph readStringGraph();

    /**
     * Reads the {@link StringGraph} from the store and calls the corresponding
     * methods of the {@link StringGraphConstructing} object to construct the 
     * StringGraph.
     * <p>
     * The same StringGraphConstructing object may be used with different
     * StringGraphStores, e.g. to create a "merged" StringGraph.
     */
    void readStringGraph(StringGraphConstructing stringGraphConstructing);
    
}
