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
import org.abego.stringgraph.core.StringGraphBuilder;
import org.junit.jupiter.api.Test;

import static org.abego.stringgraph.core.NodeTest.assertNodeEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EdgeDefaultTest {

    public static Edge getEdgeSample() {
        return getTwoEdgeSamples()[0];
    }
    
    public static Edge[] getTwoEdgeSamples() {
        StringGraphBuilder builder = StringGraphBuilderImpl.createStringGraphBuilder();
        builder.addEdge("f", "lbl", "t");
        builder.addEdge("x", "z", "y");
        return builder.build().edges().stream().sorted().toArray(Edge[]::new);
    }

    /**
     * Asserts the edges are equal to ones given in {@code expectedEdgesTextsAsLines},
     * ignoring the order.
     * <p>
     * {@code expectedEdgesTextsAsLines} has the following format:
     * <pre>
     *     {number of edges}\n
     *     {text of each edge, sorted in default order, separated by "\n"}
     * </pre>
     */
    public static void assertEdgesEqualsIgnoreOrder(String expectedEdgesTextsAsLines, Edges edges) {
        assertEquals(expectedEdgesTextsAsLines,
                edges.getSize() + "\n" +
                        IterableUtil.join("\n", edges.sortedEdgesTexts()));
    }

    @Test
    void getFromNode() {
        Edge edge = getEdgeSample();

        assertNodeEquals("f", edge.getFromNode());
    }

    @Test
    void getToNode() {
        Edge edge = getEdgeSample();

        assertNodeEquals("t", edge.getToNode());
    }

    @Test
    void getLabel() {
        Edge edge = getEdgeSample();

        assertEquals("lbl", edge.getLabel());
    }

    @Test
    void getText() {
        Edge edge = getEdgeSample();

        assertEquals("f --lbl--> t", edge.getText());
    }

    @Test
    void testEquals() {
        Edge[] edges = getTwoEdgeSamples();
        Edge edge = edges[0];
        Edge otherEdge = edges[1];

        assertEquals(edge, edge);
        assertNotEquals(edge, otherEdge);
        assertNotEquals(edge, null);
    }

    @Test
    void testHashCode() {
        Edge[] edges = getTwoEdgeSamples();

        assertNotEquals(edges[0].hashCode(), edges[1].hashCode());
    }

    @Test
    void testToString() {
        Edge edge = getEdgeSample();

        assertEquals("MyEdge{fromNode=\"f\", label=\"lbl\", toNode=\"t\"}", edge.toString());
    }

}
