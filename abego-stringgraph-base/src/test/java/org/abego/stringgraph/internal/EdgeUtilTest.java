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

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.Edges;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EdgeUtilTest {
    private final static Edges EDGES_TO_SORT = createEdgesToSort();

    private static Edges createEdgesToSort() {
        HashSet<Edge> items = new HashSet<>();
        items.add(EdgeImpl.createEdge("a", "c", "b"));
        items.add(EdgeImpl.createEdge("", "c", "b"));
        items.add(EdgeImpl.createEdge("a", "c", ""));
        items.add(EdgeImpl.createEdge("a", "", "b"));
        items.add(EdgeImpl.createEdge("a", "b", "b"));
        items.add(EdgeImpl.createEdge("a", "a", "b"));
        items.add(EdgeImpl.createEdge("a", "a", "a"));
        items.add(EdgeImpl.createEdge("b", "", "a"));
        items.add(EdgeImpl.createEdge("b", "", "b"));
        items.add(EdgeImpl.createEdge("b", "c", "b"));
        return EdgesImpl.createEdges(items);
    }

    @Test
    void constructor() {
        assertThrows(UnsupportedOperationException.class, EdgeUtil::new);
    }

    @Test
    void edgeText() {
        String edgeText = EdgeUtil.calcEdgeText(EdgeDefaultTest.getEdgeSample());

        assertEquals("f --lbl--> t", edgeText);
    }

    @Test
    void getComparator() {

        // use sorting to call "compareTo" repeatedly, avoiding writing
        // multiple "compare" calls explicitly
        Iterable<Edge> edges = EDGES_TO_SORT.sorted(EdgeUtil.getComparator());
        String actualText = IterableUtil.size(edges) + "\n" +
                IterableUtil.textOf(edges, "\n", Edge::getText);

        // the default sort order is (fromNode, label, toLabel)
        assertEquals("10\n" +
                        "\"\" --c--> b\n" +
                        "a --> b\n" +
                        "a --a--> a\n" +
                        "a --a--> b\n" +
                        "a --b--> b\n" +
                        "a --c--> \"\"\n" +
                        "a --c--> b\n" +
                        "b --> a\n" +
                        "b --> b\n" +
                        "b --c--> b",
                actualText);
    }

    @Test
    void getComparatorLabelLast() {

        // use sorting to call "compareTo" repeatedly, avoiding writing
        // multiple "compare" calls explicitly
        Iterable<Edge> edges = EDGES_TO_SORT.sorted(EdgeUtil.getComparatorLabelLast());
        String actualText = IterableUtil.size(edges) + "\n" +
                IterableUtil.textOf(edges, "\n", Edge::getText);

        // the sort order is (fromNode, toLabel, label)
        assertEquals("10\n" +
                "\"\" --c--> b\n" +
                "a --c--> \"\"\n" +
                "a --a--> a\n" +
                "a --> b\n" +
                "a --a--> b\n" +
                "a --b--> b\n" +
                "a --c--> b\n" +
                "b --> a\n" +
                "b --> b\n" +
                "b --c--> b", actualText);
    }
}
